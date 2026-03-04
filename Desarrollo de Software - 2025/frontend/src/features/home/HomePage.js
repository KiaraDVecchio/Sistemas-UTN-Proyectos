import Carousel from "../../components/carousel/Carousel";
import React, { useEffect } from "react";
import "./HomePage.css";
import { useSearchParams } from "react-router-dom";
import { getAlojamientosPaginated } from "./api/getAlojamientosPaginated";
import {
  useAlojamientos,
  useAlojamientosFilters,
  useAlojamientosTotalPages,
} from "../../context/Alojamientos";
import { MenuAccesible } from "../../components/MenuAccesible";
import { Pager } from "../../components/Pager";
import { SkeletonAlojamientos } from "./Skeleton";

export function HomePage() {
  const [params] = useSearchParams();

  const totalPages = useAlojamientosTotalPages()

  const alojamientosState = useAlojamientos();
  const [filters, setFilters] = useAlojamientosFilters();

  const page = Number.isNaN(Number(params.get("page") ?? undefined))
    ? 1
    : Number(params.get("page"));
  const ubicacion = params.get("ubicacion");
  const precioMinimo = params.get("precioMinimo");
  const precioMaximo = params.get("precioMaximo");
  const cantHuespedesPermitidos = params.get("cantHuespedesPermitidos");
  const caracteristicasEspeciales = params.get("caracteristicasEspeciales");
  const limit = params.get("limit");

  const alojamientosByPage = alojamientosState.getAlojamientoStateByPage(page);
  useEffect(() => {
    const abortController = new AbortController();
    const signal = abortController.signal;
    const _page = page;
    if (
      alojamientosByPage == null ||
      (alojamientosByPage.loading !== true &&
        alojamientosByPage.data?.length === 0)
    ) {
      alojamientosState.startFetchingAlojamientos(page);
      getAlojamientosPaginated(
        {
          ubicacion: filters.ubicacion,
          precioMinimo: filters.precioMinimo,
          precioMaximo: filters.precioMaximo,
          cantHuespedesPermitidos: filters.cantHuespedesPermitidos,
          caracteristicasEspeciales: filters.caracteristicasEspeciales,
          page,
          limit,
        },
        { signal }
      )
        .then((res) => {
          alojamientosState.receiveAlojamientos(
            res.data.data ?? [],
            _page,
            res.data.total_pages
          );
        })
        .catch(() => { });
    }
    return () => {
      // abortController.abort()
    };
  }, [filters, page, alojamientosByPage == null]);

  useEffect(() => {
    setFilters({
      ubicacion,
      precioMaximo,
      precioMinimo,
      cantHuespedesPermitidos,
      caracteristicasEspeciales,
      limit,
    });
  }, [setFilters, ubicacion, precioMaximo, precioMinimo, cantHuespedesPermitidos, caracteristicasEspeciales, limit]);

  useEffect(() => {
    alojamientosState.resetAlojamientos();
  }, [filters]);

  return (
    <div className="App">
      {/* Usamos Header con logo + navbar */}
      <MenuAccesible
        items={[
          { label: "ir a filtros", anchor: "filtros" },
          { label: "ir a contenido", anchor: "alojamientos" },
          {
            label: "Abrir menu de perfil",
            onClick: () => {
              const menu = document.getElementById("dropdown-basic");
              menu.click();
              menu.focus();
            },
          },
        ]}
      />
      <main className="alojamientos-container" id="alojamientos">
        {alojamientosByPage == null ? (
          <SkeletonAlojamientos />
        ) : alojamientosByPage.loading === true ? (
          <SkeletonAlojamientos />
        ) : alojamientosByPage.data.length === 0 ? (
          <p>No se encontraron alojamientos con esos filtros.</p>
        ) : (
          <Carousel alojamientos={alojamientosByPage.data} />
        )}
      </main>
      <Pager totalPages={totalPages} />
    </div>
  );
}
