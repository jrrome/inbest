package gal.usc.etse.inbest.model.entity;

// Transiciones válidas: PENDING -> PARTIALLY_FILLED -> FILLED, y PENDING o PARTIALLY_FILLED -> CANCELLED.
// FILLED y CANCELLED son estados finales
public enum OrderStatus {
    PENDING,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED;

    public boolean isOpen() {
        return this == PENDING || this == PARTIALLY_FILLED;
    }
}
