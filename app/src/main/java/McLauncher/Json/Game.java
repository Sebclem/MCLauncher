package McLauncher.Json;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Game {
    public String id;

    public String mainClass;

    public Assets assetIndex;

    public Download downloads;

    public ArrayList<Libraries> libraries;

    public Logging logging;

    public static class Download{
        public Libraries.Downloads.Artifact client;
    }






    public static class Libraries{
        public String name;
        public Downloads downloads;
        public List<Rules> rules = new ArrayList<>();


        public static class Downloads{
            public Artifact artifact;
            public Classifiers classifiers;

            public static class Artifact{
                public String path;
                public String sha1;
                public int size;
                public String url;
            }

            public static class Classifiers{
                @SerializedName("natives-windows-32")
                public Artifact windows32;

                @SerializedName("natives-windows-64")
                public Artifact windows64;

                @SerializedName("natives-windows")
                public Artifact windows;

                @SerializedName("natives-osx")
                public Artifact osx;

                @SerializedName("natives-linux")
                public Artifact linux;


            }
        }
    }

    public static class Assets{
        public int totalSize;
        public String url;
        public String id;
    }

    public static class Logging{
        public Client client;

        public static class Client{
            public LogFile file;
        }

        public static class LogFile{
            public String id;
            public String url;
        }
    }

    public static class Rules{
        public String action;
        public Os os;

        public static class Os{
            public String name;
            public String version;
        }
    }
}
