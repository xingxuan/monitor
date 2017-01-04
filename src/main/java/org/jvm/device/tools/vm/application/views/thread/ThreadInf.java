package org.jvm.device.tools.vm.application.views.thread;

public class ThreadInf implements Comparable {
        private int id;
        private String name;
        private int state;
        private int s1;
        private int s2;
        private int s3;
        private int s4;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getS1() {
            return s1;
        }

        public void setS1(int s1) {
            this.s1 = s1;
        }

        public int getS2() {
            return s2;
        }

        public void setS2(int s2) {
            this.s2 = s2;
        }

        public int getS3() {
            return s3;
        }

        public void setS3(int s3) {
            this.s3 = s3;
        }

        public int getS4() {
            return s4;
        }

        public void setS4(int s4) {
            this.s4 = s4;
        }

    @Override
    public int compareTo(Object o) {
        return ((ThreadInf) o).getId() - id;
    }
}