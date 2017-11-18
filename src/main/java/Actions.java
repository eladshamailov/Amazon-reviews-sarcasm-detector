public enum Actions {
    TERMINATE,
    URL,
    REVIEW;

     public static  int fromInt ( String a){
        switch (a) {
            case "TERMINATE":
                return  0;
            case "URL":
                return 1;
            case "REVIEW":
                return 2;
        }
        return -1;
    }
}


