import { MotionConfig } from "motion/react";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App";
import "./estilos/global.css";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    {/*
     * reducedMotion="user" respeita a preferência do sistema em toda animação de mola,
     * do mesmo jeito que o @media prefers-reduced-motion faz com as transições CSS.
     */}
    <MotionConfig reducedMotion="user">
      <App />
    </MotionConfig>
  </StrictMode>,
);
