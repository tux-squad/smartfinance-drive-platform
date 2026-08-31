import { getHealthStatus } from "@/application/use-cases/get-health-status";
import { NextResponse } from "next/server";

export function GET() {
  return NextResponse.json(getHealthStatus());
}
