import { Theme, ThemeOptions } from "@mui/core/styles/createMuiTheme";
import { Palette, PaletteOptions } from "@mui/core/styles/createPalette";
import {
  Typography,
  TypographyOptions,
  TypographyStyle,
} from "@mui/core/styles/createTypography";

declare module "@mui/core/styles/createPalette" {
  interface Palette {
    neutral: Record<string, string>;
  }
  interface PaletteOptions {
    neutral: Record<string, string>;
  }
}

declare module "@mui/material/styles" {
  interface TypographyVariants {
    body1Medium: React.CSSProperties;
    body2Medium: React.CSSProperties;
  }

  interface TypographyVariantsOptions {
    body1Medium: React.CSSProperties;
    body2Medium: React.CSSProperties;
  }
}

declare module "@mui/material/Typography" {
  interface TypographyPropsVariantOverrides {
    body1Medium: true;
    body2Medium: true;
  }
}

declare module "@mui/core/styles/createTheme" {
  interface Theme {
    boxShadow: Record<string, string>;
  }
  interface ThemeOptions {
    boxShadow: Record<string, string>;
  }
}
