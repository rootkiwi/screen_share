/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package build;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static build.BuildInfo.JvmBitness.NON_SUPPORTED_JVM;
import static build.BuildInfo.JvmBitness.JVM32;
import static build.BuildInfo.JvmBitness.JVM64;
import static build.BuildInfo.OperativeSystem.*;

public class BuildInfo {

    public static final Path configDirPath;
    public static final Path configFilePath;

    public static final OperativeSystem OPERATIVE_SYSTEM;
    public static final JvmBitness JVM_BITNESS;

    private static boolean builtForLinuxX86 = BuildInfo.class.getResource("/build_type/linux_x86") != null;
    private static boolean builtForLinuxX86_64 = BuildInfo.class.getResource("/build_type/linux_x86_64") != null;
    private static boolean builtForMacOsX86_64 = BuildInfo.class.getResource("/build_type/macos_x86_64") != null;
    private static boolean builtForWinX86 = BuildInfo.class.getResource("/build_type/win_x86") != null;
    private static boolean builtForWinX86_64 = BuildInfo.class.getResource("/build_type/win_x86_64") != null;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            OPERATIVE_SYSTEM = LINUX;
        } else if (os.contains("mac")) {
            OPERATIVE_SYSTEM = MACOS;
        } else if (os.contains("win")) {
            OPERATIVE_SYSTEM = WIN;
        } else {
            OPERATIVE_SYSTEM = NON_SUPPORTED_OS;
        }

        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.endsWith("86")) {
            JVM_BITNESS = JVM32;
        } else if (arch.endsWith("64")) {
            JVM_BITNESS = JVM64;
        } else {
            JVM_BITNESS = NON_SUPPORTED_JVM;
        }

        if (OPERATIVE_SYSTEM.isLinux()) {
            String configsDir;
            String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfigHome == null) {
                configsDir = Paths.get(System.getProperty("user.home"), ".config").toAbsolutePath().toString();
            } else {
                configsDir = xdgConfigHome;
            }
            configDirPath = Paths.get(configsDir, "screen_share").toAbsolutePath();
        } else if (OPERATIVE_SYSTEM.isMacOs()) {
            configDirPath = Paths.get(
                    System.getProperty("user.home"), "Library/Application Support/screen_share"
            ).toAbsolutePath();
        } else {
            configDirPath = Paths.get(
                    System.getenv("AppData"), "screen_share").toAbsolutePath();
        }
        configFilePath = Paths.get(configDirPath.toString(), "screen_share.conf");
    }

    private BuildInfo(){
    }

    public enum OperativeSystem {
        LINUX("Linux"),
        MACOS("macOS"),
        WIN("Windows"),
        NON_SUPPORTED_OS("invalid");

        public String pretty;

        OperativeSystem(String pretty) {
            this.pretty = pretty;
        }

        public boolean isLinux() {
            return this.equals(LINUX);
        }
        public boolean isMacOs() {
            return this.equals(MACOS);
        }
        public boolean isWin() {
            return this.equals(WIN);
        }
    }

    public enum JvmBitness {
        JVM32("32-bit"),
        JVM64("64-bit"),
        NON_SUPPORTED_JVM("invalid");

        public String pretty;

        JvmBitness(String pretty) {
            this.pretty = pretty;
        }

        boolean is32bit() {
            return this.equals(JVM32);
        }
        boolean is64bit() {
            return this.equals(JVM64);
        }
    }

    public static List<String> getBuiltForList() {
        List<String> builtFor = new ArrayList<>();
        if (builtForLinuxX86) {
            builtFor.add("Linux (x86) JVM 32-bit");
        }
        if (builtForLinuxX86_64) {
            builtFor.add("Linux (x86_64) JVM 64-bit");
        }
        if (builtForMacOsX86_64) {
            builtFor.add("macOS (x86_64) JVM 64-bit");
        }
        if (builtForWinX86) {
            builtFor.add("Windows (x86) JVM 32-bit");
        }
        if (builtForWinX86_64) {
            builtFor.add("Windows (x86_64) JVM 64-bit");
        }
        return builtFor;
    }

    public static boolean isBuildMadeForMe() {
        if (isLinuxX86()) {
            return builtForLinuxX86;
        } else if (isLinuxX86_64()) {
            return builtForLinuxX86_64;
        } else if (isMacOsxX86_64()) {
            return builtForMacOsX86_64;
        } else if (isWinX86()) {
            return builtForWinX86;
        } else if (isWinX86_64()) {
            return builtForWinX86_64;
        } else {
            return false;
        }
    }

    private static boolean isLinuxX86() {
        return OPERATIVE_SYSTEM.isLinux() && JVM_BITNESS.is32bit();
    }

    private static boolean isLinuxX86_64() {
        return OPERATIVE_SYSTEM.isLinux() && JVM_BITNESS.is64bit();
    }

    private static boolean isMacOsxX86_64() {
        return OPERATIVE_SYSTEM.isMacOs() && JVM_BITNESS.is64bit();
    }

    private static boolean isWinX86() {
        return OPERATIVE_SYSTEM.isWin() && JVM_BITNESS.is32bit();
    }

    private static boolean isWinX86_64() {
        return OPERATIVE_SYSTEM.isWin() && JVM_BITNESS.is64bit();
    }

    public static String getBuildVersion() {
        String version = BuildInfo.class.getPackage().getImplementationVersion();
        if (version == null) {
            return "dev";
        }
        return version;
    }

}
