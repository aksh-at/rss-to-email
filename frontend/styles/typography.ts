import { TypographyOptions } from "@mui/material/styles/createTypography";

const typography: TypographyOptions = Object.freeze({
  body1: {
    fontSize: 14,
    fontWeight: 400,
    lineHeight: 1.6,
  },
  body1Medium: {
    fontSize: 14,
    fontWeight: 500,
    lineHeight: 1.6,
  },
  body2: {
    fontSize: 12,
    fontWeight: 400,
    lineHeight: 1.4,
  },
  body2Medium: {
    fontSize: 12,
    fontWeight: 500,
    lineHeight: 1.6,
  },
  button: {
    fontSize: 12,
    fontWeight: 500,
    lineHeight: 1.6,
    textTransform: "none",
  },
  caption: {
    fontSize: 14,
    fontWeight: 500,
    lineHeight: 1.14,
  },
  fontFamily: ["DM Mono", "monospace"].join(","),
  h1: {
    fontSize: 48,
    fontWeight: 500,
    lineHeight: 1.1,
  },
  h2: {
    fontSize: 32,
    fontWeight: 500,
    lineHeight: 1.1,
  },
  h3: {
    fontSize: 24,
    fontWeight: 500,
    lineHeight: 1.1,
  },
  h4: {
    fontSize: 18,
    fontWeight: 500,
    lineHeight: 1.1,
  },
  h5: {
    fontSize: 16,
    fontWeight: 500,
    lineHeight: 1.1,
  },
  h6: {
    fontSize: 14,
    fontWeight: 500,
    lineHeight: 1.1,
  },
  overline: {
    fontSize: 12,
    lineHeight: 1.6,
    textTransform: "uppercase",
  },
  subtitle1: {
    fontSize: 12,
    fontWeight: 500,
    lineHeight: 1.6,
  },
  subtitle2: {
    fontSize: 12,
    fontWeight: 500,
    lineHeight: 1.6,
    textTransform: "uppercase",
  },
});

export default typography;
