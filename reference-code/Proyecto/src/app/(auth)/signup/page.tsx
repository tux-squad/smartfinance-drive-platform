import { SignupView } from "@/presentation/views/signup-view";

import { signupAction } from "./actions";

export default function SignupPage() {
  return <SignupView action={signupAction} />;
}
