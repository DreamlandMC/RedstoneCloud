package de.redstonecloud.cloud.commands;

import de.redstonecloud.api.util.EmptyArrays;
import de.redstonecloud.cloud.RedstoneCloud;

public class Command {

    public String cmd;

    public int argCount = 0;

    public Command(String cmd) {
        this.cmd = cmd;
    }

    protected void onCommand(String[] args) {

    }

    public String getCommand() {
        return this.cmd;
    }


    public final RedstoneCloud getServer() {
        return RedstoneCloud.getInstance();
    }

    public String[] getArgs() {
        return EmptyArrays.STRING;
    }

}