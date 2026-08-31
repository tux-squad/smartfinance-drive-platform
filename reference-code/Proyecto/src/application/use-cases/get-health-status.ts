import { createHealthStatus } from "@/domain/entities/health-status";
import { SERVICE_NAME } from "@/shared/config/app";

export function getHealthStatus() {
  return createHealthStatus(SERVICE_NAME);
}
