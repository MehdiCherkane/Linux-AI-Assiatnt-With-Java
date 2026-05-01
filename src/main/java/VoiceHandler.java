import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class VoiceHandler {

    static final String API_KEY          = "2676672cf7ac4f308609604a678b1ef5";
    static final float  SAMPLE_RATE      = 16000f;
    static final double ENERGY_THRESHOLD = 130000000; // above is speech, below is silence.
    static final int    SILENCE_LIMIT_MS = 900;
    static final int    MAX_RECORD_SECS  = 60;
    static final int    CONNECT_TIMEOUT_MS = 15000;
    static final int    READ_TIMEOUT_MS    = 30000; 
    private static final Gson gson = new Gson();

    /**
     * Records audio from the microphone, uploads to AssemblyAI, and returns
     * the transcribed text. Returns null if no speech was detected or an error occurred.
     */

    public static String recordAndTranscribe() {

        // Check for API key presence before doing anything else.
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("ASSEMBLYAI_API_KEY environment variable not set.");
            return null;
        }

        TargetDataLine mic = null;

        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            mic = (TargetDataLine) AudioSystem.getLine(info);
            mic.open(format);
            mic.start();

            // TODO: this a stuts for the core.
            System.out.println("Listening... (speak, I'll stop when you go silent)");

            byte[] buffer = new byte[4096];
            ByteArrayOutputStream audioData = new ByteArrayOutputStream();
            long silenceSince = -1;
            boolean speechDetected = false;
            long recordingStart = System.currentTimeMillis();
            long safetyDeadline = recordingStart + (MAX_RECORD_SECS * 1000L);

            while (System.currentTimeMillis() < safetyDeadline) {
                int n = mic.read(buffer, 0, buffer.length);
                if (n <= 0) {
                    break;
                }
                audioData.write(buffer, 0, n);

                // Calculate energy of this chunk
                double energy = 0;
                int samplesProcessed = n / 2;
                for (int i = 0; i < samplesProcessed * 2; i += 2) {
                    short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
                    energy += (double) sample * sample;
                }
                if (samplesProcessed > 0) {
                    energy /= samplesProcessed;
                }

                // Decide: speech or silence
                if (energy > ENERGY_THRESHOLD) {
                    speechDetected = true;
                    silenceSince = -1;

                } else {
                    if (speechDetected) {
                        if (silenceSince == -1) {
                            silenceSince = System.currentTimeMillis();
                        }

                        long silenceDuration = System.currentTimeMillis() - silenceSince;
                        if (silenceDuration >= SILENCE_LIMIT_MS) {
                            break;
                        }
                    }
                }
            }

            mic.stop();
            mic.close();
            mic = null;

            if (!speechDetected) {
                // TODO: maybe we can loop back and listen again instead of giving up immediately?

                System.out.println("No speech detected.");
                return null;
            }

            byte[] rawPcm = audioData.toByteArray();
            byte[] wavBytes = toWav(rawPcm, (int) SAMPLE_RATE, 1, 16);

            // Upload WAV
            String audioUploadUrl = uploadAudio(wavBytes);
            if (audioUploadUrl == null || audioUploadUrl.isEmpty()) {
                System.err.println("Upload failed.");
                return null;
            }


            // Request transcription
            String transcriptId = requestTranscription(audioUploadUrl);
            if (transcriptId == null || transcriptId.isEmpty()) {
                System.err.println("Transcription request failed.");
                return null;
            }
            // System.out.println("Polling for result...");

            // Poll until done
            return pollForResult(transcriptId);

        } catch (Exception e) {
            System.err.println("Error during recording/transcription: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (mic != null) {
                mic.stop();
                mic.close();
            }
        }
    }

    // ── Upload audio to AssemblyAI ───────────────────────────────────────────
    private static String uploadAudio(byte[] wavBytes) throws Exception {
        URL uploadUrl = new URL("https://api.assemblyai.com/v2/upload");
        HttpURLConnection uploadConn = (HttpURLConnection) uploadUrl.openConnection();
        uploadConn.setRequestMethod("POST");
        uploadConn.setRequestProperty("Authorization", API_KEY);
        uploadConn.setRequestProperty("Content-Type", "application/octet-stream");
        uploadConn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        uploadConn.setReadTimeout(READ_TIMEOUT_MS);
        uploadConn.setDoOutput(true);

        try (OutputStream os = uploadConn.getOutputStream()) {
            os.write(wavBytes);
        }

        String uploadResponse = readResponse(uploadConn);
        uploadConn.disconnect();
        JsonObject uploadJson = parseJson(uploadResponse);
        return getAsString(uploadJson, "upload_url");
    }

    // ── Request transcription ──────────────────────────────────────────────
    private static String requestTranscription(String audioUploadUrl) throws Exception {
        URL transcriptUrl = new URL("https://api.assemblyai.com/v2/transcript");
        HttpURLConnection transcriptConn = (HttpURLConnection) transcriptUrl.openConnection();
        transcriptConn.setRequestMethod("POST");
        transcriptConn.setRequestProperty("Authorization", API_KEY);
        transcriptConn.setRequestProperty("Content-Type", "application/json");
        transcriptConn.setRequestProperty("Accept", "application/json");
        transcriptConn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        transcriptConn.setReadTimeout(READ_TIMEOUT_MS);
        transcriptConn.setDoOutput(true);

        String transcriptBody = "{\"audio_url\": \"" + escapeJson(audioUploadUrl) + "\", \"speech_models\": [\"universal-2\"]}";
        try (OutputStream os = transcriptConn.getOutputStream()) {
            os.write(transcriptBody.getBytes(StandardCharsets.UTF_8));
        }

        String transcriptResponse = readResponse(transcriptConn);
        transcriptConn.disconnect();
        JsonObject transcriptJson = parseJson(transcriptResponse);
        return getAsString(transcriptJson, "id");
    }

    // ── Poll for transcription result ──────────────────────────────────────
    private static String pollForResult(String transcriptId) throws Exception {
        String status = "processing";
        String result = null;

        while (status.equals("processing") || status.equals("queued")) {
            Thread.sleep(1500);

            URL pollUrl = new URL("https://api.assemblyai.com/v2/transcript/" + transcriptId);
            HttpURLConnection pollConn = (HttpURLConnection) pollUrl.openConnection();
            pollConn.setRequestMethod("GET");
            pollConn.setRequestProperty("Authorization", API_KEY);
            pollConn.setRequestProperty("Accept", "application/json");
            pollConn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            pollConn.setReadTimeout(READ_TIMEOUT_MS);

            String pollResponse = readResponse(pollConn);
            pollConn.disconnect();
            JsonObject pollJson = parseJson(pollResponse);
            status = getAsString(pollJson, "status");

            if (status == null) {
                System.err.println("Unexpected response: " + pollResponse);
                return null;
            }

            // System.out.println("Status: " + status);

            if (status.equals("completed")) {
                result = getAsString(pollJson, "text");
            } else if (status.equals("error")) {
                String errorMsg = getAsString(pollJson, "error");
                System.err.println("AssemblyAI error: " + errorMsg);
                return null;
            }
        }

        if (result == null || result.isEmpty()) {
            // System.out.println("Nothing recognized.");
            return null;
        }

        // System.out.println("Heard: " + result);
        return result;
    }

    // ── Build a proper WAV file from raw PCM bytes ───────────────────────────
    static byte[] toWav(byte[] pcm, int sampleRate, int channels, int bitDepth) throws Exception {
        int byteRate   = sampleRate * channels * bitDepth / 8;
        int blockAlign = channels * bitDepth / 8;
        int dataSize   = pcm.length;
        // Ensure dataSize is even for 16-bit PCM (pad if necessary)
        int paddedDataSize = dataSize;
        if (bitDepth == 16 && (dataSize % 2) != 0) {
            paddedDataSize = dataSize + 1;
        }
        int chunkSize  = 36 + paddedDataSize;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        dos.writeBytes("RIFF");
        dos.writeInt(Integer.reverseBytes(chunkSize));
        dos.writeBytes("WAVE");
        dos.writeBytes("fmt ");
        dos.writeInt(Integer.reverseBytes(16));
        dos.writeShort(Short.reverseBytes((short) 1));
        dos.writeShort(Short.reverseBytes((short) channels));
        dos.writeInt(Integer.reverseBytes(sampleRate));
        dos.writeInt(Integer.reverseBytes(byteRate));
        dos.writeShort(Short.reverseBytes((short) blockAlign));
        dos.writeShort(Short.reverseBytes((short) bitDepth));
        dos.writeBytes("data");
        dos.writeInt(Integer.reverseBytes(paddedDataSize));
        dos.write(pcm);
        if (paddedDataSize > dataSize) {
            dos.writeByte(0); // padding
        }
        dos.flush();

        return out.toByteArray();
    }

    // ── Read HTTP response ───────────────────────────────────────────────────
    static String readResponse(HttpURLConnection conn) throws Exception {
        int status = conn.getResponseCode();
        InputStream is;
        if (status >= 200 && status < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }
        if (is == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    // ── Parse JSON field (naive but improved) ────────────────────────────────
    static JsonObject parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, JsonObject.class);
        } catch (Exception e) {
            System.err.println("Failed to parse JSON response: " + e.getMessage());
            return null;
        }
    }

    static String getAsString(JsonObject obj, String memberName) {
        if (obj == null || !obj.has(memberName) || obj.get(memberName).isJsonNull()) {
            return null;
        }
        try {
            return obj.get(memberName).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    // ── Escape a string for safe JSON inclusion ──────────────────────────────

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();

    }

    // ═══════════════════════════════════════════════════════════════════════
    // TEXT-TO-SPEECH (TTS)
    // ═══════════════════════════════════════════════════════════════════════

    public static boolean speak(String text) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("API key not set.");
            return false;
        }
        
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        try {
            // Step 1: Request TTS from AssemblyAI (returns raw PCM)
            byte[] pcmBytes = requestTTS(text);
            if (pcmBytes == null || pcmBytes.length == 0) {
                System.err.println("TTS returned empty audio.");
                return false;
            }

            // Step 2: Wrap PCM in a WAV header so Java Sound API can play it
            // AssemblyAI streaming TTS returns: 16-bit, 16kHz, mono PCM
            byte[] wavBytes = toWav(pcmBytes, 16000, 1, 16);

            // Step 3: Play using Java's built-in SourceDataLine (no external dependencies)
            playWav(wavBytes);
            return true;

        } catch (Exception e) {
            System.err.println("TTS failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static byte[] requestTTS(String text) throws Exception {
        URL url = new URL("https://api.assemblyai.com/v2/stream");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setDoOutput(true);

        // AssemblyAI streaming TTS request
        String jsonBody = "{" +
            "\"text\": \"" + escapeJson(text) + "\"," +
            "\"voice\": \"default\"" +
        "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) 
            ? conn.getInputStream() 
            : conn.getErrorStream();

        if (is == null) {
            throw new IOException("No response stream, status: " + status);
        }

        // Read all audio bytes (raw PCM from AssemblyAI)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = is.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        is.close();
        conn.disconnect();

        return baos.toByteArray();
    }

    /**
     * Plays a WAV file (with proper RIFF header) through the default speakers
     * using Java's built-in javax.sound.sampled API. No external players needed.
     */
    private static void playWav(byte[] wavBytes) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(wavBytes);
        AudioInputStream ais = AudioSystem.getAudioInputStream(bais);

        AudioFormat format = ais.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(format);
            line.start();

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = ais.read(buffer)) != -1) {
                line.write(buffer, 0, bytesRead);
            }

            line.drain(); // Wait for playback to finish
        }
        ais.close();
    }
}