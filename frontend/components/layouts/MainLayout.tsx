import React, { PropsWithChildren } from "react";
import Head from "next/head";
import { ThemeProvider, CssBaseline } from "@mui/material";
import { muiTheme } from "styles";

export function MainLayout({
  children,
}: PropsWithChildren<{}>): React.ReactElement {
  return (
    <>
      <Head>
        <title>Create Next App</title>
      </Head>

      <main>{children}</main>

      {/* <footer></footer> */}
    </>
  );
}
