import { AppProps } from "next/app";
import { muiTheme } from "styles";
import { ThemeProvider } from "@mui/material/styles";
import { CssBaseline } from "@mui/material";
import { Theme } from "@mui/material/styles";

export default function App({ Component, pageProps }: AppProps) {
  return (
    <ThemeProvider<Theme> theme={muiTheme}>
      <CssBaseline />
      <Component {...pageProps} />
    </ThemeProvider>
  );
}
