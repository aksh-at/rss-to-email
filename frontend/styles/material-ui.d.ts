import { Theme } from "@mui/material/styles";

declare module "@mui/material/styles" {
  interface TypographyVariants {
    body1Medium: React.CSSProperties;
    body2Medium: React.CSSProperties;
  }
  interface TypographyVariantsOptions {
    body1Medium: React.CSSProperties;
    body2Medium: React.CSSProperties;
  }
  interface Theme {
    boxShadow: Record<string, string>;
  }
  interface ThemeOptions {
    boxShadow: Record<string, string>;
  }
  interface Palette {
    neutral: Record<string, string>;
  }
  interface PaletteOptions {
    neutral: Record<string, string>;
  }
}

declare module "@mui/material/Typography" {
  interface TypographyPropsVariantOverrides {
    body1Medium: true;
    body2Medium: true;
  }
}

declare module "@mui/styles/defaultTheme" {
  interface DefaultTheme extends Theme {}
}
