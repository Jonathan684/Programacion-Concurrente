def cargar_datos_1(cadena2,M_pos,plazas):
    for k in cadena2:
        M_pos.append(k)
        cadena = str(k)
        subcadena = cadena[2:5]
        plazas.append(subcadena)
    return M_pos,plazas

def cargar_datos_2(cadena3):
    info = []
    for k2 in cadena3:
        info.append(k2)
    return info

def cargar_datos_3(cadena3):
    info = []
    for k2 in cadena3:
        info.append(k2)
    return info
'''def cargar_datos_4(cadena4):
    info = []
    for k2 in cadena4:
        info.append(k2)
    return info'''

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



#df2 = pd.read_html(invariantes_html)

##############################################################


# Ordenamiento de filas
def bubble_sort(nums, M_I_ordenada,M_pre_ordenada,Matriz_In_ordenada,Marcado):
    swapped = True
    while swapped:
        swapped = False
        for i in range(len(nums) - 1):
            if nums[i] > nums[i + 1]:
                # Swap the elements
                nums[i], nums[i + 1] = nums[i + 1], nums[i]
                M_I_ordenada[i], M_I_ordenada[i + 1] = M_I_ordenada[i + 1], M_I_ordenada[i]
                M_pre_ordenada[i], M_pre_ordenada[i + 1] = M_pre_ordenada[i + 1], M_pre_ordenada[i]
                Matriz_In_ordenada[i], Matriz_In_ordenada[i + 1] = Matriz_In_ordenada[i + 1], Matriz_In_ordenada[i]
                Marcado[i], Marcado[i + 1] = Marcado[i + 1], Marcado[i]
                #plazas_ordenadas[i], plazas_ordenadas[i + 1] = plazas_ordenadas[i + 1], plazas_ordenadas[i]'''
                swapped = True


# Ordenamiento de columnas
def bubble_sort2(nums,M_I_ordenada,M_pre_ordenada,Matriz_In_ordenada,filas):
    # We set swapped to True so the loop looks runs at least once
    swapped = True
    while swapped:
        swapped = False

        for i in range(len(nums) - 1):

            if nums[i] > nums[i + 1]:
                # Swap the elements
                nums[i], nums[i + 1] = nums[i + 1], nums[i]
                for fil in range(filas):
                    #Matriz_Ordenada[fil][i], Matriz_Ordenada[fil][i + 1] = Matriz_Ordenada[fil][i + 1], Matriz_Ordenada[fil][i]
                    M_pre_ordenada[fil][i], M_pre_ordenada[fil][i + 1] = M_pre_ordenada[fil][i + 1], M_pre_ordenada[fil][i]
                    M_I_ordenada[fil][i], M_I_ordenada[fil][i + 1] = M_I_ordenada[fil][i + 1],M_I_ordenada[fil][i]
                    Matriz_In_ordenada[fil][i], Matriz_In_ordenada[fil][i + 1] = Matriz_In_ordenada[fil][i + 1], Matriz_In_ordenada[fil][i]
                swapped = True

def cortar_transiciones(trasiciones):
    trasiciones = str(trasiciones).strip("[nan]")
    trasiciones = str(trasiciones).replace("'", '')
    trasiciones = str(trasiciones)[1:]
    return trasiciones
def cortar_plazas(plazas):
    plazas = str(plazas).strip("[]")
    plazas = str(plazas).replace('"', '')
    plazas = str(plazas).replace("'", '')
    plazas = str(plazas).replace(",", '')
    plazas = str(plazas).replace("P", '')
    # plazas = str(plazas).replace(" ",'')
    plazas = str(plazas)[4:]
    return plazas
def longitud_marcado(Marking):
    Marking = str(Marking[1])[4:]
    Marking = Marking.replace(" ", '')
    #Marcado_I = len(Marking) * [0]
    return Marking

def cargar_v(plaza):
    v = len(plaza) * [0]
    for i in range(len(plaza)):
        v[i] = int(plaza[i])
    return v
def limpiar_marcado(Marcado):
    Marcado = str(Marcado).strip("[]")
    Marcado = Marcado.replace(",", '')
    Marcado = Marcado.replace(" ", '')
    return Marcado

def imprimir(matriz):
    for i in matriz:
        print(i)


'''


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
plazas_ordenadas = str(plazas_ordenadas).replace(",",'')'''
'''v = len(plaza) * [0]

    for i in range(len(plaza)):
        v[i] = int(plaza[i])

    Marcado = len(Marking) * [0]
    for m in range(len(Marking)):
        Marcado[m] = (int(Marking[m]))
    # Ordenando matrices
    bubble_sort(v)
    # Cantidad de transiciones
    count = 0
    for i in trasiciones[0]:
        if (i == "T"):
            count += 1
    columnas = count
    # print("len(Marking)",len(Marking))
    filas = len(Marking)

    M_pre_ordenada = [[0] * columnas for f in range(filas)]
    M_I_ordenada = [[0] * columnas for f in range(filas)]

    for a in range(filas):
        aux_1 = str(M_pre[a]).split(" ")
        aux_2 = str(M_I[a]).split(" ")
        for b in range(columnas):
            M_pre_ordenada[a][b] = int(aux_1[b])
            M_I_ordenada[a][b] = int(aux_2[b])'''