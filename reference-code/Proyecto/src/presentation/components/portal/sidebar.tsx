"use client";

import {
  Calendar,
  Car,
  FileText,
  LayoutGrid,
  Settings,
  Shield,
  SlidersHorizontal,
  type LucideIcon,
} from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";

import { useLanguage } from "@/presentation/i18n/language-context";
import { portalDictionary } from "@/presentation/i18n/portal-dictionary";

type NavKey =
  | "dashboard"
  | "vehicles"
  | "credit"
  | "payment"
  | "metrics"
  | "simulations"
  | "settings";

type NavItem = {
  key: NavKey;
  href: string;
  icon: LucideIcon;
  enabled: boolean;
};

// "Métricas financieras" se retiró del menú: ahora VAN/TIR/TCEA se muestran
// dentro de cada simulación (sección Simulaciones).
const mainNav: NavItem[] = [
  { key: "dashboard", href: "/dashboard", icon: LayoutGrid, enabled: true },
  { key: "vehicles", href: "/vehicles", icon: Car, enabled: true },
  { key: "credit", href: "/credit", icon: SlidersHorizontal, enabled: true },
  { key: "payment", href: "/payment", icon: Calendar, enabled: true },
  { key: "simulations", href: "/simulations", icon: FileText, enabled: true },
];

const settingsNav: NavItem = {
  key: "settings",
  href: "/settings",
  icon: Settings,
  enabled: true,
};

function NavLink({
  item,
  label,
  active,
}: {
  item: NavItem;
  label: string;
  active: boolean;
}) {
  const Icon = item.icon;
  const base =
    "group relative flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition";

  if (!item.enabled) {
    return (
      <span
        className={`${base} cursor-not-allowed text-slate-300`}
        title="Coming soon"
      >
        <Icon className="h-[18px] w-[18px]" />
        {label}
      </span>
    );
  }

  return (
    <Link
      href={item.href}
      aria-current={active ? "page" : undefined}
      className={`${base} ${
        active
          ? "bg-slate-900 text-white shadow-sm"
          : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
      }`}
    >
      <Icon
        className={`h-[18px] w-[18px] transition ${
          active ? "text-white" : "text-slate-400 group-hover:text-slate-600"
        }`}
      />
      {label}
    </Link>
  );
}

export function Sidebar() {
  const pathname = usePathname();
  const { lang } = useLanguage();
  const t = portalDictionary[lang];

  return (
    <aside className="sticky top-0 flex h-screen w-64 shrink-0 flex-col self-start border-r border-slate-200 bg-white">
      <div className="flex items-center gap-3 px-6 py-5">
        <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-slate-900 text-white shadow-sm">
          <Shield className="h-[18px] w-[18px]" />
        </span>
        <div className="leading-tight">
          <p className="text-sm font-bold tracking-tight text-slate-900">Autify</p>
          <p className="text-xs text-slate-400">{t.portalSubtitle}</p>
        </div>
      </div>

      <nav className="flex flex-1 flex-col gap-1 overflow-y-auto px-3 py-4">
        <p className="px-3 pb-2 text-[11px] font-semibold uppercase tracking-wider text-slate-400">
          {t.menu}
        </p>
        {mainNav.map((item) => (
          <NavLink
            key={item.key}
            item={item}
            label={t.nav[item.key]}
            active={pathname === item.href}
          />
        ))}
      </nav>

      <div className="border-t border-slate-100 px-3 py-3">
        <NavLink
          item={settingsNav}
          label={t.nav[settingsNav.key]}
          active={pathname === settingsNav.href}
        />
      </div>
    </aside>
  );
}
