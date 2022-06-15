import os
import glob
from argparse import ArgumentParser, ArgumentError, RawTextHelpFormatter
import dominate
from dominate.tags import *
import pandas as pd
import math

class_list = pd.DataFrame()
field_list = pd.DataFrame()
method_list = pd.DataFrame()

def call_java(filepath):
    os.popen('javac Compiler.java')
    if filepath.endswith(".java\""):
        stream = os.popen('java Compiler ' + filepath)
        output = stream.read()

    return output

def create_page(html_title, class_list, field_list, method_list):
    table_headers = ['Accessibility and Type', 'Field Name', 'Documentation']
    table_headers_m = ['Accessibility and Type', 'Field Name', 'Documentation', 'Parameters']
    doc = dominate.document(title='Java Documentation for {}'.format(html_title))

    with doc.head:
        link(rel='stylesheet', href='../miniJava/style.css')

    with doc:
        with div(cls='container'):
            for i in range(len(class_list)):
                specific_clist = class_list.iloc[i].tolist()
                h1("Class: " + specific_clist[0])

                with div(cls='class_descriptor'):
                    cdescriptor = specific_clist[4].replace('$',',').split('       ')
                    for ele in cdescriptor:
                        p(ele)

                br()
                with div(id='field'):
                    with table(id='main', cls='table table-striped'):
                        caption(h3('Field Summary'))
                        with thead():
                            with tr():
                                for table_head in table_headers:
                                    th(table_head)

                        with tbody():
                            for i in range(len(field_list)):
                                specific_flist = field_list.iloc[i].tolist()
                                a_t = ''
                                if(specific_flist[5] == 'false'):
                                    a_t = 'Public, '
                                else:
                                    a_t = 'Private, '

                                if(specific_flist[6] is 'true'):
                                    a_t = a_t + 'Static, '

                                a_t = a_t + str(specific_flist[3]).lower()

                                with tr():
                                    td(a_t)
                                    td(specific_flist[0])
                                    td(specific_flist[4])
                br()
                with div(id='method'):
                    with table(id='main', cls='table table-striped'):
                        caption(h3('Method Summary'))
                        with thead():
                            with tr():
                                for table_head in table_headers_m:
                                    th(table_head)

                        with tbody():
                            for i in range(len(method_list)):
                                specific_mlist = method_list.iloc[i].tolist()
                                a_t = ''
                                if(specific_mlist[5] == 'false'):
                                    a_t = 'Public, '
                                else:
                                    a_t = 'Private, '

                                if(specific_mlist[6] is 'true'):
                                    a_t = a_t + 'Static, '

                                a_t = a_t + str(specific_mlist[3]).lower()

                                param = ''
                                if specific_mlist[7] != specific_mlist[7]:
                                    param = '---'
                                else:
                                    param = specific_mlist[7]

                                with tr():
                                    td(a_t)
                                    td(specific_mlist[0])
                                    td(specific_mlist[4])
                                    td(param)

    f = open('../Output_files/{}.html'.format(html_title), 'w')
    f.write(str(doc))
    f.close()

def main():
    parser = ArgumentParser()
    parser.add_argument("-make-docs", "--create-documentation",
        help="Enter directory containing the Java code", type = str)
    args = parser.parse_args()

    filepath = "\"" + args.create_documentation + "\""

    result = call_java(filepath)

    html_title = filepath.split("/")[-1].split(".")[0]

    df = pd.read_csv('../Output_files/{}.csv'.format(html_title))
    class_list = df.loc[df['Class/Field/Method'] == 'Class']
    field_list = df.loc[df['Class/Field/Method'] == 'Field']
    method_list = df.loc[df['Class/Field/Method'] == 'Method']

    create_page(html_title, class_list, field_list, method_list)
    os.remove('../Output_files/{}.csv'.format(html_title))


if __name__ == "__main__":
    main()
