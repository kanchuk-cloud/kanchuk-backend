ALTER TABLE products
    ADD COLUMN blouse_fabric_id UUID REFERENCES fabrics(id),
    ADD COLUMN dupatta_fabric_id UUID REFERENCES fabrics(id);
