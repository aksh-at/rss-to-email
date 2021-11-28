import { createTheme, responsiveFontSizes } from "@mui/material";
import boxShadow from "styles/boxShadow";
import palette from "styles/palette";
import typography from "styles/typography";
import zIndex from "styles/zIndex";

export const muiTheme = responsiveFontSizes(
  createTheme(
    {
      boxShadow,
      palette,
      props: {
        MuiButtonBase: {
          disableRipple: true,
        },
      },
      spacing: (factor) => `${factor * 0.5}rem`,
      typography,
    },
    { zIndex }
  )
);
