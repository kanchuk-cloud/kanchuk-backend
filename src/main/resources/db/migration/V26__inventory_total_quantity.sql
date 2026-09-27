-- total_quantity records the original stock added to inventory (never decremented on sales)
ALTER TABLE inventory_levels ADD COLUMN total_quantity INT NOT NULL DEFAULT 0;

-- Seed existing rows: treat current quantity_on_hand as the initial total
UPDATE inventory_levels SET total_quantity = quantity_on_hand WHERE total_quantity = 0;
