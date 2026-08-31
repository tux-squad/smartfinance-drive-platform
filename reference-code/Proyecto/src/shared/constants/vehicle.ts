import type {
  VehicleCondition,
  VehicleCurrency,
} from "@/domain/entities/vehicle";

export const VEHICLE_CONDITIONS: Array<{
  value: VehicleCondition;
  label: string;
}> = [
  { value: "new", label: "New" },
  { value: "used", label: "Used" },
];

export const VEHICLE_CONDITION_VALUES: VehicleCondition[] =
  VEHICLE_CONDITIONS.map((condition) => condition.value);

export const VEHICLE_CURRENCIES: Array<{
  value: VehicleCurrency;
  label: string;
}> = [
  { value: "PEN", label: "PEN" },
  { value: "USD", label: "USD" },
];

export const VEHICLE_CURRENCY_VALUES: VehicleCurrency[] =
  VEHICLE_CURRENCIES.map((currency) => currency.value);

export enum VehicleBrandOption {
  Toyota = "Toyota",
  Kia = "Kia",
  Hyundai = "Hyundai",
  Nissan = "Nissan",
  Chevrolet = "Chevrolet",
  Suzuki = "Suzuki",
  Mazda = "Mazda",
  Volkswagen = "Volkswagen",
  Ford = "Ford",
}

export const VEHICLE_BRANDS: Array<{
  value: VehicleBrandOption;
  label: string;
}> = [
  { value: VehicleBrandOption.Toyota, label: "Toyota" },
  { value: VehicleBrandOption.Kia, label: "Kia" },
  { value: VehicleBrandOption.Hyundai, label: "Hyundai" },
  { value: VehicleBrandOption.Nissan, label: "Nissan" },
  { value: VehicleBrandOption.Chevrolet, label: "Chevrolet" },
  { value: VehicleBrandOption.Suzuki, label: "Suzuki" },
  { value: VehicleBrandOption.Mazda, label: "Mazda" },
  { value: VehicleBrandOption.Volkswagen, label: "Volkswagen" },
  { value: VehicleBrandOption.Ford, label: "Ford" },
];

export const VEHICLE_BRAND_IMAGE: Record<VehicleBrandOption, string> = {
  [VehicleBrandOption.Toyota]: "/cars/Toyota.jpg",
  [VehicleBrandOption.Kia]: "/cars/Kia.jpg",
  [VehicleBrandOption.Hyundai]: "/cars/Hyundai.webp",
  [VehicleBrandOption.Nissan]: "/cars/Nissan.webp",
  [VehicleBrandOption.Chevrolet]: "/cars/Chevrolet.jpg",
  [VehicleBrandOption.Suzuki]: "/cars/Suzuki.jpg",
  [VehicleBrandOption.Mazda]: "/cars/Mazda.jpg",
  [VehicleBrandOption.Volkswagen]: "/cars/Volkswagen.png",
  [VehicleBrandOption.Ford]: "/cars/Ford.jpg",
};

export const VEHICLE_MODELS_BY_BRAND: Record<VehicleBrandOption, string[]> = {
  [VehicleBrandOption.Toyota]: ["Hilux", "Corolla Cross", "RAV4", "Yaris"],
  [VehicleBrandOption.Kia]: ["Sportage", "Seltos", "Rio", "Picanto"],
  [VehicleBrandOption.Hyundai]: ["Tucson", "Creta", "Elantra", "Accent"],
  [VehicleBrandOption.Nissan]: ["Frontier", "Kicks", "Sentra", "Versa"],
  [VehicleBrandOption.Chevrolet]: ["Tracker", "Onix", "N400", "Captiva"],
  [VehicleBrandOption.Suzuki]: ["Swift", "Vitara", "S-Presso", "Baleno"],
  [VehicleBrandOption.Mazda]: ["CX-5", "CX-30", "Mazda 3", "BT-50"],
  [VehicleBrandOption.Volkswagen]: ["T-Cross", "Nivus", "Amarok", "Virtus"],
  [VehicleBrandOption.Ford]: ["Ranger", "Territory", "Explorer", "Escape"],
};
