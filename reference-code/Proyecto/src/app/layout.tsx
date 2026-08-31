import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "App Finanzas",
  description: "Aplicacion fullstack para gestion financiera.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="es" className="h-full antialiased">
      <body className="min-h-full">{children}</body>
    </html>
  );
}
