# Assets de la pantalla de auth (login / registro)

El layout `src/app/(auth)/layout.tsx` muestra un video de carros en la mitad
derecha de la pantalla. Coloca aqui tus archivos con estos nombres exactos:

- `cars.mp4`  -> video principal (formato MP4 / H.264). **Recomendado.**
- `cars.webm` -> (opcional) misma version en WebM para mejor compresion.
- `cars-poster.jpg` -> imagen fija que se muestra mientras carga el video
  (y como respaldo si el navegador no puede reproducirlo).

## Recomendaciones para el video
- Duracion corta (10-20 s) y en loop, sin audio (se reproduce en `muted`).
- Resolucion 1080p (1920x1080) vertical u horizontal; se recorta con `object-cover`.
- Peso: intenta mantenerlo por debajo de ~5 MB para que cargue rapido.
- Sitios con videos libres de derechos: Pexels, Pixabay, Coverr.

Mientras no exista el video, el panel muestra un gradiente oscuro de respaldo,
asi que la pantalla nunca se ve rota.
