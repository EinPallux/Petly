package com.pallux.petly.model;

import java.util.UUID;

public class DustChamberSlot {
    private final int slotIndex;
    private UUID petInstanceId;

    public DustChamberSlot(int slotIndex) {
        this.slotIndex = slotIndex;
        this.petInstanceId = null;
    }

    public DustChamberSlot(int slotIndex, UUID petInstanceId) {
        this.slotIndex = slotIndex;
        this.petInstanceId = petInstanceId;
    }

    public int getSlotIndex() { return slotIndex; }
    public UUID getPetInstanceId() { return petInstanceId; }
    public void setPetInstanceId(UUID petInstanceId) { this.petInstanceId = petInstanceId; }
    public boolean isEmpty() { return petInstanceId == null; }
}
