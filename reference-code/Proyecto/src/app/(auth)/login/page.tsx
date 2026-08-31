import { LoginView } from "@/presentation/views/login-view";

import { loginAction } from "./actions";

export default function LoginPage() {
  return <LoginView action={loginAction} />;
}
