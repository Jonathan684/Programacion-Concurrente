import re
archivo = open("log.txt")
line = ["",""]

linea = archivo.readline()
line[0] = linea
line[1] = 0
print('====== Expresion Regular ========')
print('[',line[0],',',line[1],']')
while(1):
     line = re.subn('(((T1)(.*?)((T2)(.*?)(T4)|(T3)(.*?)(T5))(.*?)(T6))|((T7)(.*?)(T8)(.*?)(T9)(.*?)(T0)))',
                    '\g<4>\g<7>\g<10>\g<12>\g<16>\g<18>\g<20>',line[0])
     print(line)
     if(line[1] == 0 ):
         break

if(len(line[0])==0):
    print('El test termino OK')
else:
    print('Sobran transiciones')
