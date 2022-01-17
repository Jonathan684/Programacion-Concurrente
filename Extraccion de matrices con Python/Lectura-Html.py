import pandas as pd
import modulo as md
#######################################
matrices_html = 'MATRICES.html'
#invariantes_html= 'invariantes.html'
#Matriz_Pos = open("M.Pos.txt", "w")
Matriz_Pre = open("M.Pre.txt", "w")
Matriz_Inhibicion = open("M.H.txt", "w")
Matriz_Indicencia = open("M.I.txt", "w")
Marcado_Inicial = open("VMI.txt", "w")

#InvTrans_txt = open("InvTrans.txt", "w")
#Transiciones_txt = open("Transiciones.txt", "w")
#Plazas_txt = open("Plazas.txt", "w")
#print('Longitud inicial',len(df))

def principal():
    df = pd.read_html(matrices_html)
    M_pos = []
    M_pre = []
    M_I = []
    Matriz_In = []
    Marking = []
    trasiciones = []
    plazas = []
    count = 0
    #invariantes_T = []
    for i in range(len(df)):
        if (i == 1):  # incidence matrix I+
            M_pos,plazas = md.cargar_datos_1(df[i].values,M_pos,plazas)
        if (i == 3):  # matrix pre I-
            M_pre = md.cargar_datos_2(df[i].values)
        if (i == 5):  # matrix I
            M_I = md.cargar_datos_2(df[i].values)
        if (i == 7):  # matrix H
            Matriz_In = md.cargar_datos_2(df[i].values)
        if (i == 9):  # marcado
            Marking = md.cargar_datos_3(df[i].values)
    trasiciones.append(M_pos[0])
    #M_pos = md.contenido_importante(M_pos)
    M_pre = md.contenido_importante(M_pre)
    M_I = md.contenido_importante(M_I)
    Matriz_In = md.contenido_importante(Matriz_In)

    trasiciones[0] = md.cortar_transiciones(trasiciones[0])
    plazas_ordenadas = (len(plazas) - 1) * [0]
    for u in range(len(plazas) - 1):
        plazas_ordenadas[u] = str(plazas[u + 1])
    plazas = md.cortar_plazas(plazas)
    Marking = md.contenido_importante(Marking)
    plaza = str(plazas).split(" ")
    vector_plazas_desordenadas = md.cargar_v(plaza)
    Marking = md.longitud_marcado(Marking)
    Marcado = len(Marking) * [0]
    for m in range(len(Marking)):
        Marcado[m] = (int(Marking[m]))

    t = trasiciones[0].split(" ")
    for i in trasiciones[0]:
        if (i == "T"):
            count += 1
    columnas = count
    filas = len(Marking)
    vector_transiciones_desordenadas = [0] * columnas
    for tr in range(columnas):
        vector_transiciones_desordenadas[tr] = int(str(t[tr].replace("T", '')))
    M_pre_ordenada = [[0] * columnas for f in range(filas)]
    M_I_ordenada = [[0] * columnas for f in range(filas)]
    Matriz_H_ordenada = [[0] * columnas for f in range(filas)]
    for a in range(filas):
        aux_1 = str(M_pre[a]).split(" ")
        aux_2 = str(M_I[a]).split(" ")
        aux_3 = str(Matriz_In[a]).split(" ")
        for b in range(columnas):
            M_pre_ordenada[a][b] = int(aux_1[b])
            M_I_ordenada[a][b] = int(aux_2[b])
            Matriz_H_ordenada[a][b] = int(aux_3[b])

    # Ordenando matrices
    md.bubble_sort(vector_plazas_desordenadas,M_I_ordenada,M_pre_ordenada,Matriz_H_ordenada,Marcado) # Ordenamiento de filas
    md.bubble_sort2(vector_transiciones_desordenadas, M_I_ordenada,M_pre_ordenada,Matriz_H_ordenada, filas) # Ordenamiento de columnas
    Marcado = md.limpiar_marcado(Marcado)

    md.escribir_archivo(Matriz_Pre, M_pre_ordenada)
    md.escribir_archivo(Matriz_Indicencia, M_I_ordenada)
    md.escribir_archivo(Matriz_Inhibicion, Matriz_H_ordenada)

    md.escribir_archivo2(Marcado_Inicial, Marcado)
    print("Marcado")
    print(Marcado)
    print("M_I_ordenada")
    md.imprimir(M_I_ordenada)
    print("="*50)
    print("M_pre_ordenada")
    md.imprimir(M_pre_ordenada)
    print("=" * 50)
    print("Matriz_H_ordenada")
    md.imprimir(Matriz_H_ordenada)
    
if __name__ == '__main__':
    principal()

