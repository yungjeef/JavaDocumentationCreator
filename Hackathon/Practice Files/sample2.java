class sample2 {
    """
        @Author: Jeffrey Mei
        @Collaborator: Trevor Phillips, Franklin Clinton, Michael De Santa
        @Date: October 29, 2020
        This class simulates a calculator 
    """

    int a; """This field is the first number"""
    int b;
    int e; """This field will not be used"""

    public int add() {
        """ this function adds two numbers together 3"""
        return a + b;
    }

    public int subtract() {
        """ this function subtracts two numbers together 3"""
        return a - b;
    }

    public int multiply() {
        """ this function multiplies two numbers together 3"""
        return a * b;
    }

    public int divide() {
        """ this function divides first number by the second number 3"""
        if(b == 0) {
          return 0;
        }
        return a/b;
    }

    public void changeNumber(int c, int d) {
      a = c * d;
    }
}
