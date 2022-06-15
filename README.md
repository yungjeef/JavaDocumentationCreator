# Java Documentation Creator

I created the Java Documentation Creator as part of the LLNL Fall 2020 Hackathon. I was inspired by some Python tools such as Sphinx which could generate documentation for files and I wanted to do the same for Java. There were already tools out there which could do this but I wanted to add a twist to it which would change the way Java code could be compiled.

In Java, the compiler does not recognize ` """ """ `. In Python, ` """ """ ` is frequently used for writing docstrings. I wanted to introduce that to Java. I tweaked an old school project where I had to create a Java compiler to accept the triple quotation marks. The user would provide a path of a Java file to a Python program which would then send the file to the Java backend. Then all the information about the classes, fields, methods from that Java file would be passed back up to Python and it would be written in an aesthetic HTML format.

Here is an example:

![Sample code](../sample_code.png)

Would be turned into...

![HTML Display 1](../html1.png)

![HTML Display 2](../html2.png)