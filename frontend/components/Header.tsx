import React, { useState, useEffect } from "react";
import { Typography, Button, useMediaQuery, Fade } from "@mui/material";
import { Box, useTheme } from "@mui/system";
import { Theme } from "@mui/material/styles";
import MenuIcon from "@mui/icons-material/Menu";
import CloseIcon from "@mui/icons-material/Close";

export const HEADER_HEIGHT = 12;

export function Header(): React.ReactElement {
  const theme = useTheme<Theme>();
  const smOrUp = useMediaQuery(theme.breakpoints.up("sm"));
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => setMobileMenuOpen(false), [smOrUp]);

  const buttonStyles = {
    padding: theme.spacing(1, 2),
    boxShadow: "none",
    backgroundColor: "#2958F0",
    "&:hover": {
      backgroundColor: "#003cf0",
      boxShadow: theme.boxShadow.medium,
    },
    borderRadius: theme.spacing(1),
  };

  const logo = (
    <>
      <Typography
        sx={{ color: theme.palette.neutral.darkgrey }}
        component="span"
        variant="h4"
      >
        RSS
      </Typography>
      <Typography
        sx={{ color: theme.palette.neutral.black }}
        component="span"
        variant="h4"
      >
        /
      </Typography>
      <Typography
        sx={{ color: theme.palette.neutral.lightgrey }}
        component="span"
        variant="h4"
      >
        EMAIL
      </Typography>
    </>
  );
  return (
    <>
      <Box
        sx={{
          p: 3.5,
          display: "flex",
          justifyContent: "space-between",
          color: theme.palette.neutral.black,
          position: "relative",
          top: 0,
          height: theme.spacing(HEADER_HEIGHT),
          alignItems: "flex-start",
          minHeight: theme.spacing(HEADER_HEIGHT),
        }}
      >
        <Box sx={{ display: "flex" }}>
          <Box sx={{ mr: 5 }}>{logo}</Box>
          {smOrUp && (
            <Box sx={{ mr: 2 }}>
              <Typography variant="body1Medium">
                POPULAR (Coming Soon!)
              </Typography>
            </Box>
          )}
        </Box>
        {smOrUp && (
          <Button variant="contained" disableRipple sx={buttonStyles}>
            <Typography variant="body1Medium">MANAGE EMAILS</Typography>
          </Button>
        )}
        {!smOrUp && (
          <Box onClick={() => setMobileMenuOpen(true)} sx={{ mt: -0.5 }}>
            <MenuIcon />
          </Box>
        )}
      </Box>

      {!smOrUp && (
        <Fade in={mobileMenuOpen}>
          <Box
            sx={{
              backgroundColor: theme.palette.neutral.white,
              height: "100vh",
              position: "fixed",
              width: "100vw",
              zIndex: 1,
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
            }}
          >
            <Box
              sx={{
                width: "100%",
                display: "flex",
                justifyContent: "space-between",
                p: 3.5,
              }}
            >
              <div>{logo}</div>
              <CloseIcon
                onClick={() => setMobileMenuOpen(false)}
                sx={{ mt: -0.5 }}
              />
            </Box>
            <Box sx={{ my: 4 }}>
              <Typography variant="body1Medium">
                POPULAR (Coming soon!)
              </Typography>
            </Box>
            <Button
              variant="contained"
              disableRipple
              sx={{ ...buttonStyles, width: "fit-content" }}
            >
              <Typography variant="body1Medium">MANAGE EMAILS</Typography>
            </Button>
          </Box>
        </Fade>
      )}
    </>
  );
}
