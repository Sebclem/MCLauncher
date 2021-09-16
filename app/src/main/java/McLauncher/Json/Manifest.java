package McLauncher.Json;

import java.util.ArrayList;

public class Manifest {

    public Latest latest;
    public ArrayList<VersionItem> versions;




    public class VersionItem{
        public String id;
        public String type;
        public String url;
        public String time;
        public String releaseTime;

    }

    public class Latest{
        public String release;
        public String snapshot;
    }
}


