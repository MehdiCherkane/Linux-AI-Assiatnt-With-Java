import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class VoiceTest {

    static final String API_KEY            = "2676672cf7ac4f308609604a678b1ef5";
    static final float  SAMPLE_RATE        = 16000f;
    static final double ENERGY_THRESHOLD   = 100000000;  // above this = speech
    static final int    SILENCE_LIMIT_MS   = 1500;    // stop after 1.5s of silence
    static final int    MAX_RECORD_SECS    = 10;      // safety cap

    public static void main(String[] args) throws Exception {

        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info);
        mic.open(format);
        mic.start();

        System.out.println("Listening... (speak, I'll stop when you go silent)");

        byte[] buffer             = new byte[4096];
        ByteArrayOutputStream audioData = new ByteArrayOutputStream();
        long silenceSince         = -1;      // timestamp when silence started (-1 = not silent)
        boolean speechDetected    = false;   // did we hear anything yet?
        long recordingStart       = System.currentTimeMillis();
        long safetyDeadline       = recordingStart + (MAX_RECORD_SECS * 1000L);

        while (System.currentTimeMillis() < safetyDeadline) {
            int n = mic.read(buffer, 0, buffer.length);
            audioData.write(buffer, 0, n);

            // ── Calculate energy of this chunk ───────────────────────────────
            double energy = 0;
            for (int i = 0; i < n - 1; i += 2) {
                // reconstruct 16-bit sample from two bytes (little-endian)
                short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
                energy += (double) sample * sample;
            }
            energy /= (n / 2.0); // average over number of samples

            // ── Decide: speech or silence ────────────────────────────────────
            if (energy > ENERGY_THRESHOLD) {
                speechDetected = true;
                silenceSince   = -1; // reset silence timer
                System.out.print("█"); // visual feedback
            } else {
                if (speechDetected) { // only count silence AFTER speech started
                    if (silenceSince == -1) {
                        silenceSince = System.currentTimeMillis(); // start silence timer
                    }
                    System.out.print("░");
                    long silenceDuration = System.currentTimeMillis() - silenceSince;
                    if (silenceDuration >= SILENCE_LIMIT_MS) {
                        System.out.println("\nSilence detected, stopping.");
                        break;
                    }
                }
            }
        }

        mic.stop();
        mic.close();

        if (!speechDetected) {
            System.out.println("No speech detected.");
            return;
        }

        byte[] rawPcm  = audioData.toByteArray();
        byte[] wavBytes = toWav(rawPcm, (int) SAMPLE_RATE, 1, 16);
        System.out.println("Recorded " + wavBytes.length + " bytes. Uploading...");

        // ── Step 2: Upload WAV ───────────────────────────────────────────────
        URL uploadUrl = new URL("https://api.assemblyai.com/v2/upload");
        HttpURLConnection uploadConn = (HttpURLConnection) uploadUrl.openConnection();
        uploadConn.setRequestMethod("POST");
        uploadConn.setRequestProperty("Authorization", API_KEY);
        uploadConn.setRequestProperty("Content-Type", "application/octet-stream");
        uploadConn.setDoOutput(true);

        try (OutputStream os = uploadConn.getOutputStream()) {
            os.write(wavBytes);
        }

        String uploadResponse = readResponse(uploadConn);
        String audioUploadUrl = parseField(uploadResponse, "upload_url");

        if (audioUploadUrl.isEmpty()) {
            System.out.println("Upload failed: " + uploadResponse);
            return;
        }
        System.out.println("Uploaded. Requesting transcription...");

        // ── Step 3: Request transcription ────────────────────────────────────
        URL transcriptUrl = new URL("https://api.assemblyai.com/v2/transcript");
        HttpURLConnection transcriptConn = (HttpURLConnection) transcriptUrl.openConnection();
        transcriptConn.setRequestMethod("POST");
        transcriptConn.setRequestProperty("Authorization", API_KEY);
        transcriptConn.setRequestProperty("Content-Type", "application/json");
        transcriptConn.setDoOutput(true);

        String transcriptBody = "{\"audio_url\": \"" + audioUploadUrl + "\", \"speech_models\": [\"universal-2\"]}";
        try (OutputStream os = transcriptConn.getOutputStream()) {
            os.write(transcriptBody.getBytes());
        }

        String transcriptResponse = readResponse(transcriptConn);
        String transcriptId = parseField(transcriptResponse, "id");

        if (transcriptId.isEmpty()) {
            System.out.println("Transcription request failed: " + transcriptResponse);
            return;
        }
        System.out.println("Polling for result...");

        // ── Step 4: Poll until done ──────────────────────────────────────────
        String status = "processing";
        String result = "";

        while (status.equals("processing") || status.equals("queued")) {
            Thread.sleep(1500);

            URL pollUrl = new URL("https://api.assemblyai.com/v2/transcript/" + transcriptId);
            HttpURLConnection pollConn = (HttpURLConnection) pollUrl.openConnection();
            pollConn.setRequestMethod("GET");
            pollConn.setRequestProperty("Authorization", API_KEY);

            String pollResponse = readResponse(pollConn);
            status = parseField(pollResponse, "status");
            System.out.println("Status: " + status);

            if (status.equals("completed")) {
                result = parseField(pollResponse, "text");
            } else if (status.equals("error")) {
                System.out.println("AssemblyAI error: " + parseField(pollResponse, "error"));
                return;
            }
        }

        if (result.isEmpty()) {
            System.out.println("Nothing recognized.");
        } else {
            System.out.println("Heard: " + result);
        }
    }

    // ── Build a proper WAV file from raw PCM bytes ───────────────────────────
    static byte[] toWav(byte[] pcm, int sampleRate, int channels, int bitDepth) throws Exception {
        int byteRate   = sampleRate * channels * bitDepth / 8;
        int blockAlign = channels * bitDepth / 8;
        int dataSize   = pcm.length;
        int chunkSize  = 36 + dataSize;

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
        dos.writeInt(Integer.reverseBytes(dataSize));
        dos.write(pcm);

        return out.toByteArray();
    }

    static String readResponse(HttpURLConnection conn) throws Exception {
        int status = conn.getResponseCode();
        InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    static String parseField(String json, String field) {
        String key = "\"" + field + "\"";
        int start = json.indexOf(key);
        if (start == -1) return "";
        start += key.length();
        while (start < json.length() && (json.charAt(start) == ':' || json.charAt(start) == ' ')) start++;
        if (start >= json.length() || json.charAt(start) != '"') return "";
        start++;
        int end = json.indexOf('"', start);
        if (end == -1) return "";
        return json.substring(start, end);
    }
}
