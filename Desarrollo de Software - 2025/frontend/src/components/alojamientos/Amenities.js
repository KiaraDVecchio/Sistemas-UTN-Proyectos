export function Amenities({ alojamiento }) {
  return (
    <>

      <div className="amenities-list">
        {alojamiento.caracteristicas.map((item, idx) => (
          <div key={idx} className="amenity-item">
            {item.replace(/_/g, " ")}
          </div>
        ))}
      </div>
    </>
  );
}
