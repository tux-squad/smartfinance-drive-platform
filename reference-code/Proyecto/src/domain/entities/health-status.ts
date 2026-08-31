export type HealthStatus = {
  status: "ok";
  service: string;
};

export function createHealthStatus(service: string): HealthStatus {
  return {
    status: "ok",
    service,
  };
}
