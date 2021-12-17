import pandas as pd
#######################################
matrices_html = 'info_red_petri.html'
#invariantes_html= 'invariantes.html'

#Matriz_Pos = open("M.Pos.txt", "w")
Matriz_Pre = open("M.Pre.txt", "w")
Matriz_Indicencia = open("M.I.txt", "w")
Marcado_Inicial = open("VMI.txt", "w")
#InvTrans_txt = open("InvTrans.txt", "w")


Transiciones_txt = open("Transiciones.txt", "w")
Plazas_txt = open("Plazas.txt", "w")

M_pos = []
M_pre = []
M_I = []
Marking = []
trasiciones = []
plazas = []
invariantes_T =[]
#print('Longitud inicial',len(df))
def mostrar(cadena2):
    for k in cadena2:
        M_pos.append(k)
        cadena = str(k)
        subcadena = cadena[2:5]
        plazas.append(subcadena)

def mostrar2(cadena3):
    info = []
    for k2 in cadena3:
        info.append(k2)
    return info

def mostrar3(cadena3):
    info = []
    for k2 in cadena3:
        info.append(k2)
    return info
def mostrar4(cadena4):
    info = []
    for k2 in cadena4:
        info.append(k2)
    return info

def contenido_importante(data):
    for r in range(len(data)):
        ##print(i)
        data[r] = str(data[r]).strip("[nan]")
        data[r] = str(data[r]).replace("'", '')
        data[r] = str(data[r])[3:]
    for k in range(len(data)):
        data[k] = str(data[k]).strip(" ")
    data.remove(data[0])  ## Removemos la primera fila ya que son las transiciones que estan en el vecto transiciones
    return data
def contenido_importante2(data):
    for r in range(len(data)):
        ##print(i)
        data[r] = str(data[r]).strip("[]")
        data[r] = str(data[r]).replace("'", '')
        data[r] = str(data[r]).replace(",", '')
        data[r] = str(data[r])[8:]

    for k in range(len(data)):
        data[k] = str(data[k]).strip(" ")
    for t in range(len(data)):
        data[t] = str(data[t]).strip(" ")
    data.remove(data[0])  ## Removemos la primera fila ya que son las transiciones que estan en el vecto transiciones
    #print(data[0])
    return data

def escribir_archivo(nombre_del_archivo,data):
    for line in data:
        line = str(line).replace(",", '')
        nombre_del_archivo.write(str(line).strip("[]"))
        nombre_del_archivo.write("\n")
    nombre_del_archivo.close()

def escribir_archivo2(nombre_del_arch,data):
    for line in data:
        nombre_del_arch.write(line)
        nombre_del_arch.write("\n")
    nombre_del_arch.close()


df = pd.read_html(matrices_html)
#df2 = pd.read_html(invariantes_html)

##############################################################
for i in range(len(df)):
    #print("Tabla numero :", i)
    #print(df[i].values)
    if(i==1):# incidence matrix I+
        #print(df[i].values)
        mostrar(df[i].values)
    if(i==3):# matrix pre I+
        #print(df[i].values)
        M_pre = mostrar2(df[i].values)
    if (i == 5):# matrix I
        # print(df[i].values)
        M_I = mostrar2(df[i].values)
    if (i == 9):# matrix I
        # print(df[i].values)
        Marking = mostrar3(df[i].values)
#############################################################

'''for j in range(len(df2)):
    if(j==0):
        ##print("Invariatnes Transicion")
        invariantes_T = mostrar2(df2[j].values)
        invariantes_T = contenido_importante2(invariantes_T)'''

#############################################################

trasiciones.append(M_pos[0])

M_pos = contenido_importante(M_pos)
M_pre = contenido_importante(M_pre)
M_I   = contenido_importante(M_I)

trasiciones[0]= str(trasiciones[0]).strip("[nan]")
trasiciones[0]= str(trasiciones[0]).replace("'",'')
trasiciones[0]= str(trasiciones[0])[1:]

t = len(trasiciones[0])*[0]

#print(plazas)
plazas_ordenadas = (len(plazas)-1)*[0]

for u in range(len(plazas)-1):
    plazas_ordenadas[u]=str(plazas[u+1])


plazas = str(plazas).strip("[]")
plazas = str(plazas).replace('"','')
plazas = str(plazas).replace("'",'')
plazas = str(plazas).replace(",",'')
plazas = str(plazas).replace("P",'')
#plazas = str(plazas).replace(" ",'')

plazas = str(plazas)[4:]


Marking = contenido_importante(Marking)
Marking = str(Marking[1])[4:]


Marking = Marking.replace(" ",'')
Marcado_I = len(Marking)*[0]
plaza = str(plazas).split(" ")

v = len(plaza)*[0]

for i in range(len(plaza)):
    v[i] = int(plaza[i])


#print("v : ",v)
Marcado = len(Marking)*[0]
for m in range(len(Marking)):
    Marcado[m]=(int(Marking[m]))

#escribir_archivo(InvTrans_txt,invariantes_T)

# Ordenamiento de filas
def bubble_sort(nums):
    swapped = True
    while swapped:
        swapped = False
        for i in range(len(nums) - 1):
            if nums[i] > nums[i + 1]:
                # Swap the elements
                nums[i], nums[i + 1] = nums[i + 1], nums[i]
                M_pre[i], M_pre[i + 1] = M_pre[i + 1], M_pre[i]
                M_pos[i], M_pos[i + 1] = M_pos[i + 1], M_pos[i]
                M_I[i], M_I[i + 1] = M_I[i + 1], M_I[i]
                Marcado[i], Marcado[i + 1] = Marcado[i + 1], Marcado[i]
                plazas_ordenadas[i], plazas_ordenadas[i + 1] = plazas_ordenadas[i + 1], plazas_ordenadas[i]
                swapped = True

#Ordenando matrices
bubble_sort(v)
#Cantidad de transiciones
count = 0
for i in trasiciones[0]:
    if(i=="T"):
        count +=1
columnas = count
#print("len(Marking)",len(Marking))
filas = len(Marking)

M_pre_ordenada = [[0] * columnas for f in range(filas)]
M_I_ordenada = [[0] * columnas for f in range(filas)]


for a in range(filas):
    aux_1 = str(M_pre[a]).split(" ")
    aux_2 = str(M_I[a]).split(" ")
    for b in range(columnas):
        M_pre_ordenada[a][b] = int(aux_1[b])
        M_I_ordenada[a][b] = int(aux_2[b])

# Ordenamiento de columnas
def bubble_sort2(nums):
    # We set swapped to True so the loop looks runs at least once
    swapped = True
    while swapped:
        swapped = False

        for i in range(len(nums) - 1):

            if nums[i] > nums[i + 1]:
                # Swap the elements
                nums[i], nums[i + 1] = nums[i + 1], nums[i]
                for fil in range(filas):
                    M_pre_ordenada[fil][i], M_pre_ordenada[fil][i + 1] = M_pre_ordenada[fil][i + 1], M_pre_ordenada[fil][i]
                    M_I_ordenada[fil][i], M_I_ordenada[fil][i + 1] = M_I_ordenada[fil][i + 1],M_I_ordenada[fil][i]
                swapped = True

Marcado = str(Marcado).strip("[]")
Marcado= Marcado.replace(",",'')
Marcado= Marcado.replace(" ",'')


t = trasiciones[0].split(" ")
vector_transiciones = [0]*columnas

for tr in range(columnas):
    vector_transiciones [tr] = int(str(t[tr].replace("T",'')))


bubble_sort2(vector_transiciones)

print("-"*40)
print("Matriz Pre ordenada")
print("-"*40)
for i in range(filas):
  print(M_pre_ordenada[i])
print("-"*50)
print("Matriz I ordenada")
print("-"*40)
for i in range(filas):
  print(M_I_ordenada[i])
print("-"*40)
#print("m_pre",type(M_pre[0][2]))
###############################################################
#escribir_archivo(Matriz_Pos, M_pos) ##No se necesita por ahora
escribir_archivo(Matriz_Pre, M_pre_ordenada)
escribir_archivo(Matriz_Indicencia, M_I_ordenada)
escribir_archivo2(Marcado_Inicial,Marcado)

#escribir_archivo(Plazas_txt,plazas_ordenadas)
#############################################################

Marcado_I = str(Marcado_I).strip("[]")
Marcado_I = Marcado_I.replace(",",'')
Marcado_I = Marcado_I.replace(" ",'')

plazas_ordenadas = str(plazas_ordenadas).strip("[]")
plazas_ordenadas = str(plazas_ordenadas).replace('"','')
plazas_ordenadas = str(plazas_ordenadas).replace("'",'')
plazas_ordenadas = str(plazas_ordenadas).replace(",",'')

###############################################################
#Transiciones y plazas ordenadas
Plazas_txt.write(str(plazas_ordenadas))
Plazas_txt.write("\n")
Plazas_txt.close()

vector_transiciones = str(vector_transiciones).strip("[]")
vector_transiciones = str(vector_transiciones).replace(",",'')
Transiciones_txt.write(str(vector_transiciones))
Transiciones_txt.write("\n")
Transiciones_txt.close()
################################################################