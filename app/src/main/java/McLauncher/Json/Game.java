package McLauncher.Json;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Game {
    public String id;

    public String mainClass;

    public Assets assetIndex;

    public Download downloads;

    public ArrayList<Libraries> libraries;

    public Logging logging;


    public class Download{
        public Libraries.Downloads.Artifact client;
    }






    public class Libraries{
        public String name;
        public Downloads downloads;



        public class Downloads{
            public Artifact artifact;

            public Classifiers classifiers;



            public class Artifact{
                public String path;
                public String sha1;
                public int size;
                public String url;
            }

            public class Classifiers{
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

    public class Assets{
        public int totalSize;
        public String url;
        public String id;
    }

    public class Logging{
        public Client client;

        public class Client{
            public LogFile file;
        }

        public class LogFile{
            public String id;
            public String url;
        }
    }

}
