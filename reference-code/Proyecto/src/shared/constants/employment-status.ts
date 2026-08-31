export const EMPLOYMENT_STATUSES = [
  { value: "employed", label: "Employed" },
  { value: "self_employed", label: "Self-employed" },
  { value: "unemployed", label: "Unemployed" },
  { value: "student", label: "Student" },
  { value: "retired", label: "Retired" },
] as const;

export type EmploymentStatus = (typeof EMPLOYMENT_STATUSES)[number]["value"];

export const EMPLOYMENT_STATUS_VALUES: readonly string[] =
  EMPLOYMENT_STATUSES.map((status) => status.value);
