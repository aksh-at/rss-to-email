import { MainLayout } from "components/layouts/MainLayout";
import { Header } from "components/Header";
import { Box, useTheme } from "@mui/system";
import { Button, Typography, useMediaQuery } from "@mui/material";
import { Theme } from "@mui/material/styles";
import Image from "next/image";
import { useState } from "react";

function Home(): React.ReactElement {
  const theme = useTheme<Theme>();
  const lgOrUp = useMediaQuery(theme.breakpoints.up("lg"));
  const mdOrUp = useMediaQuery(theme.breakpoints.up("md"));

  const [emailFocus, setEmailFocus] = useState(false);
  const [rssUrlFocus, setRssUrlFocus] = useState(false);

  const emailStyles = {
    borderBottom: `5px solid ${theme.palette.neutral.black}`,
    borderLeft: "none",
    borderRight: "none",
    borderTop: "none",
    "&:focus": {
      outline: "none",
    },
    ...theme.typography.h2,
    paddingBottom: theme.spacing(0.5),
    background: "transparent",
  };

  const buttonStyles = {
    padding: theme.spacing(2, 3),
    boxShadow: "none",
    border: `3px solid ${theme.palette.neutral.black}`,
    "&:hover": {
      backgroundColor: theme.palette.neutral.white,
      boxShadow: theme.boxShadow.medium,
      color: theme.palette.neutral.black,
    },
    borderRadius: theme.spacing(1.5),
    backgroundColor: theme.palette.neutral.black,
  };

  return (
    <MainLayout>
      <Box sx={{ height: "100vh", display: "flex", flexDirection: "column" }}>
        <Image
          alt="Decorate squiggle"
          src="/static/gradient-background.jpg"
          layout="fill"
        />
        <Header />
        <Box
          sx={{
            flexGrow: 1,
            display: "flex",
            alignItems: "center",
            zIndex: 0,
            mb: 10,
          }}
        >
          <Box
            sx={{ p: 4, ml: mdOrUp ? 16 : "auto", mr: mdOrUp ? 32 : "auto" }}
          >
            <Box sx={{ display: lgOrUp ? "flex" : "block", mb: 3 }}>
              <Box sx={{ flexShrink: 0 }}>
                <Typography variant="h1">Send email updates to</Typography>
              </Box>
              <Box
                component="input"
                sx={{
                  ...emailStyles,
                  ml: lgOrUp ? 4 : 0,
                  mb: lgOrUp ? 0 : 4,
                  mt: lgOrUp ? 0 : 1,
                  height: theme.spacing(8),
                  width: "100%",
                }}
                placeholder={emailFocus ? "Enter email" : ""}
                onFocus={() => setEmailFocus(true)}
                onBlur={() => setEmailFocus(false)}
              />
            </Box>
            <Box sx={{ display: lgOrUp ? "flex" : "block", mb: 5 }}>
              <Box sx={{ flexShrink: 0 }}>
                <Typography variant="h1">from feed:</Typography>
              </Box>
              <Box
                component="input"
                sx={{
                  ...emailStyles,
                  ml: lgOrUp ? 4 : 0,
                  mb: lgOrUp ? 0 : 4,
                  mt: lgOrUp ? 0 : 1,
                  height: theme.spacing(8),
                  width: "100%",
                }}
                placeholder={rssUrlFocus ? "Enter RSS or Atom feed URL" : ""}
                onFocus={() => setRssUrlFocus(true)}
                onBlur={() => setRssUrlFocus(false)}
              />
            </Box>
            <Button sx={buttonStyles} variant="contained" disableRipple>
              <Typography variant="h2">{"Subscribe >"}</Typography>
            </Button>
          </Box>
        </Box>
      </Box>
    </MainLayout>
  );
}

export default Home;
