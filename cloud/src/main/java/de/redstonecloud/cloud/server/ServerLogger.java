package de.redstonecloud.cloud.server;

import de.redstonecloud.cloud.RedstoneCloud;
import de.redstonecloud.cloud.scheduler.task.TaskHandler;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

@Builder
@Log4j2
public class ServerLogger extends Thread {
    @Getter
    private Server server;
    @lombok.Builder.Default
    private boolean running = true;
    @lombok.Builder.Default
    private File logFile = null;
    @lombok.Builder.Default
    private boolean logToConsole = false;
    @lombok.Builder.Default
    private BufferedWriter writer = null;
    @lombok.Builder.Default
    private DummyErrorReader errorReader = null;
    @lombok.Builder.Default
    private TaskHandler<?> writerTask = null;
    @lombok.Builder.Default
    private Set<String> lastMessages = new HashSet<>();

    public void run() {
        logFile = new File(server.getDirectory() + "/buffer_console.log");
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        errorReader = DummyErrorReader.builder()
                .logger(this)
                .build();

        errorReader.start();

        // Prepare writer for logging file
        writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(logFile));
            lastMessages.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Flush writer every 5 seconds
        this.writerTask = RedstoneCloud.getInstance().getScheduler().scheduleRepeatingTask(() -> {
            try {
                if (writer != null) writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 5000);

        // Start logging
        while (running && server.getProcess() != null && server.getProcess().getInputStream() != null) {
            BufferedReader out = new BufferedReader(new InputStreamReader(server.getProcess().getInputStream()));
            String line = "";

            try {
                while (running && (line = out.readLine()) != null) {
                    if (logToConsole) log.info("[" + server.getName() + "]", line);
                    if (writer != null) {
                        try {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException ignore) {
                        }
                    }

                    lastMessages.add(line);
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    public void cancel() {
        running = false;
        if (writerTask != null) writerTask.cancel();
        server.setLogger(null);
        this.errorReader.cancel();
        try {
            if (writer != null) writer.flush();
            if (writer != null) writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (logToConsole) RedstoneCloud.getInstance().setCurrentLogServer(null);

        this.interrupt();
    }

    public void enableConsoleLogging() {
        //output all file content to console
        try {
            Set<String> sent = new HashSet<>(); // TODO: Handle
            BufferedReader reader = new BufferedReader(new FileReader(logFile.getPath()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[" + server.getName() + "]", line);
                sent.add(line);
            }

            //output all lastMessages
            for (String msg : lastMessages) {
                if (!sent.contains(msg)) {
                    log.info("[" + server.getName() + "]", msg);
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logToConsole = true;
    }

    public void disableConsoleLogging() {
        logToConsole = false;
    }

    public boolean isConsoleLogging() {
        return logToConsole;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public void addMessage(String msg) {
        lastMessages.add(msg);
    }
}