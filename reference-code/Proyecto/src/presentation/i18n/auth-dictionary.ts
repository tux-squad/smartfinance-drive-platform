import type { AuthFormErrorCode } from "@/shared/types/auth-form-state";

export type Language = "es" | "en";

/**
 * Textos de las pantallas de autenticación (login / registro) en cada idioma.
 * El idioma por defecto es español; el switcher permite cambiar a inglés.
 */
export const authDictionary = {
  es: {
    // Marca / encabezados
    loginSubtitle: "Inicia sesión para continuar",
    signupSubtitle: "Crea tu cuenta para empezar",

    // Campos
    emailLabel: "Correo electrónico",
    emailPlaceholder: "nombre@empresa.com",
    passwordLabel: "Contraseña",
    passwordPlaceholder: "Tu contraseña",
    passwordHint: "Mínimo 6 caracteres",
    confirmPasswordLabel: "Confirmar contraseña",
    confirmPasswordPlaceholder: "Repite tu contraseña",
    remember: "Mantener sesión iniciada",

    // Botones
    signIn: "Iniciar sesión",
    signingIn: "Iniciando sesión...",
    createAccount: "Crear cuenta",
    creatingAccount: "Creando cuenta...",

    // Enlaces inferiores
    noAccount: "¿Aún no tienes cuenta?",
    createOne: "Regístrate",
    haveAccount: "¿Ya tienes cuenta?",
    goSignIn: "Inicia sesión",

    // Panel del video
    heroTitle: "Gestiona tu flota con Autify",
    heroSubtitle:
      "Financiamiento, simulación de crédito y administración de vehículos en una sola plataforma.",

    // Pie
    footer: "Plataforma segura para concesionarios.",

    // Mensajes de error
    errors: {
      invalid_email: "Ingresa un correo electrónico válido.",
      password_required: "La contraseña es obligatoria.",
      invalid_credentials: "Correo o contraseña incorrectos.",
      sign_in_failed: "No se pudo iniciar sesión. Inténtalo de nuevo.",
      password_too_short: "La contraseña debe tener al menos 6 caracteres.",
      passwords_mismatch: "Las contraseñas no coinciden.",
      email_taken: "Ya existe una cuenta con este correo.",
      sign_up_failed: "No se pudo crear la cuenta. Inténtalo de nuevo.",
    } satisfies Record<AuthFormErrorCode, string>,
  },
  en: {
    loginSubtitle: "Sign in to continue",
    signupSubtitle: "Create your account to get started",

    emailLabel: "Email",
    emailPlaceholder: "name@company.com",
    passwordLabel: "Password",
    passwordPlaceholder: "Your password",
    passwordHint: "At least 6 characters",
    confirmPasswordLabel: "Confirm password",
    confirmPasswordPlaceholder: "Repeat your password",
    remember: "Keep me signed in",

    signIn: "Sign in",
    signingIn: "Signing in...",
    createAccount: "Create account",
    creatingAccount: "Creating account...",

    noAccount: "Don't have an account?",
    createOne: "Sign up",
    haveAccount: "Already have an account?",
    goSignIn: "Sign in",

    heroTitle: "Manage your fleet with Autify",
    heroSubtitle:
      "Financing, credit simulation and vehicle management in a single platform.",

    footer: "Secure platform for dealerships.",

    errors: {
      invalid_email: "Enter a valid email address.",
      password_required: "Password is required.",
      invalid_credentials: "Invalid email or password.",
      sign_in_failed: "Could not sign in. Please try again.",
      password_too_short: "Password must be at least 6 characters.",
      passwords_mismatch: "Passwords do not match.",
      email_taken: "An account with this email already exists.",
      sign_up_failed: "Could not create the account. Please try again.",
    } satisfies Record<AuthFormErrorCode, string>,
  },
} as const;

export type AuthTexts = (typeof authDictionary)[Language];
