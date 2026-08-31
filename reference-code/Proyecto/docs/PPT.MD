# Estructura del PowerPoint — Trabajo Final

> Guion de diapositivas para la exposición (enfoque **académico/comercial**).
> El PPT **no repite el informe**: resume, muestra y convence. Regla de oro:
> *una idea por diapositiva, imagen/diagrama > texto, y dejar hablar al demo.*
>
> Duración objetivo: **25 min** de exposición + rueda de preguntas.
> Basado en el índice de `INFORME.MD` y en el estado real del sistema.

---

## Mapa rápido (18–20 diapositivas)

| # | Diapositiva | Tiempo | Prioridad |
|---|-------------|--------|-----------|
| 1 | Presentacion e Integrantes | 0:30 | — | Emily 
| 2 | El problema / oportunidad | 1:30 | ⭐ | Emily 
| 3 | Nuestra solución (pitch) | 1:30 | ⭐⭐⭐ | Emily
| 4 | Marco legal y teórico (síntesis) | 2:00 | ⭐⭐ | Emily
| 5 | Método Francés + fórmulas clave | 2:30 | ⭐⭐⭐ | Olimpo
| 6 | Modelo de base de datos | 1:30 | ⭐⭐ | Olimpo
| 8 | Arquitectura del sistema | 2:00 | ⭐⭐⭐ | Gael
| 9 | Stack tecnológico y despliegue | 1:30 | ⭐⭐ | Gael
| 10 | **DEMO en vivo  | 5:00 | ⭐⭐⭐ | Oliver
| 11 | Conclusiones | 1:00 | ⭐ | Gael

---

## Detalle diapositiva por diapositiva

### 1 · Portada
- Nombre comercial del producto + logo, curso, sección, integrantes, ciclo 2026-10.
- Una frase-gancho: *"Compra Inteligente vehicular, calculada al centavo."*

### 2 · El problema / la oportunidad ⭐
- El crédito vehicular en Perú es opaco: el cliente no entiende TCEA, seguros ni la cuota balón.
- **Dato de impacto** (1 cifra grande): p. ej. brecha entre TEA publicada y TCEA real.
- Enmarca por qué hace falta una herramienta de transparencia.

### 3 · Nuestra solución (pitch de 1 slide) ⭐⭐⭐
- Qué hace la app en 3 bullets: simula plan de pagos francés, calcula VAN/TIR/TCEA, compara entidades.
- Captura "hero" de la pantalla más vistosa (Indicadores o Configuración de Crédito).
- Diferenciadores: multimoneda (S/ y $), tasa nominal/efectiva, períodos de gracia.

### 4 · Objetivo del Estudiante (Student Outcome)
- ABET SO-1 / ICACIT A y E: *identificar, formular y resolver* un problema financiero con ciencia y matemática.
- 1 línea por outcome, sin copiar el informe entero.

### 5 · Marco legal y teórico (síntesis) ⭐⭐
- **Legal:** Ley General del Sistema Financiero, transparencia SBS. (logos/íconos, no párrafos).
- **Teórico:** crédito vehicular, método francés, TEA/TNA + capitalización.
- Cita las normas como respaldo (una diapositiva, no cinco).

### 6 · Método Francés + fórmulas clave ⭐⭐⭐
> **La diapositiva más importante académicamente.** Muestra que dominan la matemática.
- Cuota constante:  `A = P·i·(1+i)ⁿ / ((1+i)ⁿ − 1)`
- Conversión de tasa:  `TEM = (1+TEA)^(1/12) − 1`  ·  `TEA = (1+TNA/m)^m − 1`
- VAN y TIR (óptica del deudor), definidos en 1 línea cada uno.
- Menciona: *"todas centralizadas en un único módulo `credit-calculator.ts`"*.

### 7 · Arquitectura del sistema ⭐⭐⭐
- Diagrama de capas: **Presentación → Aplicación → Dominio → Infraestructura**.
- Resalta que las fórmulas viven en Dominio (puro, testeable, sin UI ni BD).
- Mensaje: *"si cambia una fórmula, se toca un solo archivo"* (mantenibilidad).

### 8 · Modelo de base de datos ⭐⭐
- Diagrama entidad-relación: `profiles, vehicles, financial_entities, simulations, payment_schedules, rate_benchmarks`.
- Destaca RLS (seguridad por usuario) y `rate_benchmarks` (transparencia comparativa).

### 9 · Análisis de datos (Entrada / Salida / Intermedios)
- Tabla compacta: entradas (precio, inicial, balón, plazo, tasa, gracia), intermedios (TEM, capital), salidas (cuota, VAN, TIR, TCEA, cronograma).

### 10 · Algoritmo del cronograma ⭐⭐
- Diagrama de flujo (o Nassi-Schneiderman) del armado del plan de pagos.
- Incluye la bifurcación de **períodos de gracia** total/parcial.

### 11 · Datos de prueba y validación ⭐⭐⭐
- 2 juegos de datos (soles y dólares) → cálculo a mano vs. sistema.
- Tabla "esperado vs obtenido" = **prueba de veracidad del modelo** (lo pide el enunciado).

### 12–15 · DEMO ⭐⭐⭐
> Lo que más pesa en la exposición. Si el demo en vivo es riesgoso, usa capturas/video embebido.
1. Login + registro de cliente/vehículo.
2. Configuración del crédito (moneda, tasa, gracia).
3. **Cronograma de pagos** generado.
4. **Indicadores** VAN / TIR / TCEA + comparación de entidades.
- Muestra los **tooltips de ayuda contextual** (requisito explícito del enunciado).

### 16 · Stack tecnológico y despliegue ⭐⭐
- Next.js 16 + React, TypeScript, Supabase (Postgres + Auth), Prisma, Tailwind.
- Servicio de alojamiento y, si está desplegada, **la URL** (o QR).

### 17 · Requerimientos de hardware
- Cliente: cualquier navegador moderno. Servidor: serverless/Supabase.
- 3 bullets, sin sobrecargar.

### 18 · Ventaja competitiva / enfoque comercial ⭐⭐
- Comparación de tasas entre entidades = valor para el vendedor y para el cliente.
- Multimoneda, multi-entidad, transparencia SBS "de fábrica".

### 19 · Conclusiones y trabajo futuro
- Qué se logró (arquitectura, fórmulas, BD, auth).
- Roadmap honesto: cablear cálculos reales al 100 %, exportar PDF del cronograma, historial de simulaciones.

### 20 · Cierre
- "Gracias" + datos de contacto/repo + **QR al demo o al video**.

---

## Recomendaciones de diseño (alto impacto)
- **Máx. ~30 palabras por diapositiva.** Lo demás lo dice quien expone.
- Paleta consistente con la app (slate + acentos esmeralda) para reforzar marca.
- Toda fórmula, en imagen o editor de ecuaciones (no como texto plano).
- Numerar diapositivas y ensayar tiempos: **25 min es poco**, sobra contenido.
- Vestimenta formal y cámaras encendidas (secciones a distancia) — lo exige el enunciado.

## ⚠️ Antes de exponer — coherencia con el código
Según `ANALISIS-CUMPLIMIENTO.MD`, hoy la TCEA/cuota son mock y el cronograma no se
genera. **No afirmar en el PPT que esos cálculos ya funcionan end-to-end** si no
se corrigen antes; presentarlos como diseño implementado en el dominio y en
proceso de cableado, o —mejor— corregir C1–C3 antes de la demo.
