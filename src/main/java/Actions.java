public enum Actions {
    TERMINATE,
    URL,
    REVIEW;

     public static  int fromInt ( String a){
        switch (a) {
            case "TERMINATE":
                return  1;
            case "URL":
                return 2;
            case "REVIEW":
                return 3;
        }
        return -1;
    }
}


