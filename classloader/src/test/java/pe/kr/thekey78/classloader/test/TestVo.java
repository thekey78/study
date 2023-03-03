package pe.kr.thekey78.classloader.test;

public class TestVo {
    public void print(String name) {
        System.out.println("Instance method modified, Hello World " + name + " : " + getClass().getClassLoader());
    }

    public static void printStatic(String name) {
        System.out.println("Static method modified, Hello World " + name + " : " + TestVo.class.getClassLoader());
    }
}
