package com.smartfinance.smartfinancedriveplatform.catalog.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.FinancialEntityId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.UserId;
import com.smartfinance.smartfinancedriveplatform.catalog.domain.model.valueobjects.VehicleId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;

import lombok.Getter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Vehicle aggregate root.
 * Represents a vehicle registered in the catalog for financing.
 */
@Getter
public class Vehicle extends AbstractDomainAggregateRoot<Vehicle> {

    private final VehicleId id;
    private UserId userId;
    private FinancialEntityId financialEntityId;
    private String brand;
    private String model;
    private int manufactureYear;
    private String condition; // "NEW" or "USED"
    private Money price;
    private String imagePath;
    private String status; // "ACTIVE", "SOLD", "RESERVED"
    private Integer mileage;
    private String transmission;
    private String engine;
    private String traction;
    private List<String> images = new ArrayList<>();

    /**
     * Constructor for reconstituting the aggregate from persistence.
     */
    public Vehicle(VehicleId id, UserId userId, FinancialEntityId financialEntityId, 
                   String brand, String model, int manufactureYear, String condition, 
                   Money price, String imagePath, String status, Integer mileage,
                   String transmission, String engine, String traction, List<String> images) {
        this.id = id;
        this.userId = userId;
        this.financialEntityId = financialEntityId;
        this.brand = brand;
        this.model = model;
        this.manufactureYear = manufactureYear;
        this.condition = condition;
        this.price = price;
        this.imagePath = imagePath;
        setStatus(status);
        setMileage(mileage);
        setTransmission(transmission);
        setEngine(engine);
        setTraction(traction);
        setImages(images);
    }

    public Vehicle(VehicleId id, UserId userId, FinancialEntityId financialEntityId, 
                   String brand, String model, int manufactureYear, String condition, 
                   Money price, String imagePath, String status, Integer mileage,
                   String transmission, String engine, String traction) {
        this(id, userId, financialEntityId, brand, model, manufactureYear, condition, price, imagePath, status, mileage, transmission, engine, traction, new ArrayList<>());
    }

    public Vehicle(VehicleId id, UserId userId, FinancialEntityId financialEntityId, 
                   String brand, String model, int manufactureYear, String condition, 
                   Money price, String imagePath) {
        this(id, userId, financialEntityId, brand, model, manufactureYear, condition, price, imagePath, "ACTIVE", 0, null, null, null, new ArrayList<>());
    }

    /**
     * Constructor for creating a new Vehicle.
     */
    public Vehicle(UserId userId, FinancialEntityId financialEntityId, String brand, 
                   String model, int manufactureYear, String condition, Money price, 
                   String imagePath, String status, Integer mileage, String transmission,
                   String engine, String traction, List<String> images) {
        this.id = new VehicleId(UUID.randomUUID());
        setUserId(userId);
        setFinancialEntityId(financialEntityId);
        setBrand(brand);
        setModel(model);
        setManufactureYear(manufactureYear);
        setCondition(condition);
        setPrice(price);
        setImagePath(imagePath);
        setStatus(status);
        setMileage(mileage);
        setTransmission(transmission);
        setEngine(engine);
        setTraction(traction);
        setImages(images);
    }

    public Vehicle(UserId userId, FinancialEntityId financialEntityId, String brand, 
                   String model, int manufactureYear, String condition, Money price, 
                   String imagePath, String status, Integer mileage, String transmission,
                   String engine, String traction) {
        this(userId, financialEntityId, brand, model, manufactureYear, condition, price, imagePath, status, mileage, transmission, engine, traction, new ArrayList<>());
    }

    public Vehicle(UserId userId, FinancialEntityId financialEntityId, String brand, 
                   String model, int manufactureYear, String condition, Money price, 
                   String imagePath) {
        this(userId, financialEntityId, brand, model, manufactureYear, condition, price, imagePath, "ACTIVE", 0, null, null, null, new ArrayList<>());
    }

    public void setUserId(UserId userId) {
        if (userId == null) {
            throw new DomainValidationException("catalog.error.vehicle.userId.required");
        }
        this.userId = userId;
    }

    public void setFinancialEntityId(FinancialEntityId financialEntityId) {
        if (financialEntityId == null) {
            throw new DomainValidationException("catalog.error.vehicle.financialEntityId.required");
        }
        this.financialEntityId = financialEntityId;
    }

    public void setBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            throw new DomainValidationException("catalog.error.vehicle.brand.required");
        }
        this.brand = brand.trim();
    }

    public void setModel(String model) {
        if (model == null || model.isBlank()) {
            throw new DomainValidationException("catalog.error.vehicle.model.required");
        }
        this.model = model.trim();
    }

    public void setManufactureYear(int manufactureYear) {
        if (manufactureYear < 1900) {
            throw new DomainValidationException("catalog.error.vehicle.manufactureYear.invalid");
        }
        this.manufactureYear = manufactureYear;
    }

    public void setCondition(String condition) {
        if (condition == null || condition.isBlank()) {
            throw new DomainValidationException("catalog.error.vehicle.condition.required");
        }
        String normalized = condition.trim().toUpperCase();
        if (!normalized.equals("NEW") && !normalized.equals("USED")) {
            throw new DomainValidationException("catalog.error.vehicle.condition.invalid");
        }
        this.condition = normalized;
    }

    public void setPrice(Money price) {
        if (price == null) {
            throw new DomainValidationException("catalog.error.vehicle.price.required");
        }
        this.price = price;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath != null ? imagePath.trim() : null;
    }

    public void setStatus(String status) {
        if (status == null || status.isBlank()) {
            this.status = "ACTIVE";
            return;
        }
        String normalized = status.trim().toUpperCase();
        if (!normalized.equals("ACTIVE") && !normalized.equals("SOLD") && !normalized.equals("RESERVED")) {
            throw new DomainValidationException("catalog.error.vehicle.status.invalid");
        }
        this.status = normalized;
    }

    public void setMileage(Integer mileage) {
        if (mileage != null && mileage < 0) {
            throw new DomainValidationException("catalog.error.vehicle.mileage.invalid");
        }
        this.mileage = mileage;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission != null ? transmission.trim() : null;
    }

    public void setEngine(String engine) {
        this.engine = engine != null ? engine.trim() : null;
    }

    public void setTraction(String traction) {
        this.traction = traction != null ? traction.trim() : null;
    }

    public void setImages(List<String> images) {
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
    }

    public void addImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            if (this.images == null) {
                this.images = new ArrayList<>();
            }
            if (!this.images.contains(imageUrl.trim())) {
                this.images.add(imageUrl.trim());
            }
        }
    }

    public void updateStatus(String status) {
        setStatus(status);
    }

    /**
     * Updates details of the vehicle.
     */
    public void updateDetails(FinancialEntityId financialEntityId, String brand, String model, 
                               int manufactureYear, String condition, Money price, String imagePath,
                               String status, Integer mileage, String transmission, String engine,
                               String traction, List<String> images) {
        setFinancialEntityId(financialEntityId);
        setBrand(brand);
        setModel(model);
        setManufactureYear(manufactureYear);
        setCondition(condition);
        setPrice(price);
        setImagePath(imagePath);
        setStatus(status);
        setMileage(mileage);
        setTransmission(transmission);
        setEngine(engine);
        setTraction(traction);
        if (images != null) {
            setImages(images);
        }
    }

    public void updateDetails(FinancialEntityId financialEntityId, String brand, String model, 
                               int manufactureYear, String condition, Money price, String imagePath) {
        updateDetails(financialEntityId, brand, model, manufactureYear, condition, price, imagePath, this.status, this.mileage, this.transmission, this.engine, this.traction, this.images);
    }
}

