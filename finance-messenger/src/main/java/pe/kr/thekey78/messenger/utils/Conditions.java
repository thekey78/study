package pe.kr.thekey78.messenger.utils;

public class Conditions {
    public static class IF {
        boolean contition;
        public IF(boolean contition) {
            this.contition = contition;
        }

        public static Then if_(boolean condition) {
            return new Then(condition);
        }
    }

    public static class Then {
        private boolean condition;

        public Then(boolean condition) {
            this.condition = condition;
        }

        public Else then(Runnable runnable) {
            if (condition) {
                runnable.run();
                return new Else(false);
            } else {
                return new Else(true);
            }
        }
    }

    public static class Else {
        private boolean executed;

        public Else(boolean executed) {
            this.executed = executed;
        }

        public void else_(Runnable runnable) {
            if (!executed) {
                runnable.run();
            }
        }
    }
}
