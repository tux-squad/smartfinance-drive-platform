import { redirect } from "next/navigation";

export default function Home() {
  // El area autenticada es el punto de entrada. El middleware redirige a /login
  // si no hay sesion.
  redirect("/dashboard");
}
