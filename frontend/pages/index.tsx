import { Typography } from "@mui/material";
import { MainLayout } from "components/layouts/MainLayout";

export default function Home(): React.ReactElement {
  return (
    <MainLayout>
      <div>
        <Typography variant="h1">Hello World</Typography>
      </div>
      <div>
        <Typography variant="h2">Hello World</Typography>
      </div>
      <div>
        <Typography variant="h3">Hello World</Typography>
      </div>
      <div>
        <Typography variant="h4">Hello World</Typography>
      </div>
      <div>
        <Typography variant="h5">Hello World</Typography>
      </div>
      <div>
        <Typography variant="h6">Hello World</Typography>
      </div>
      <div>
        <Typography variant="subtitle1">Hello World</Typography>
      </div>
      <div>
        <Typography variant="subtitle2">Hello World</Typography>
      </div>
      <div>
        <Typography variant="body1">Hello World</Typography>
      </div>
      <div>
        <Typography variant="body1Medium">Hello World</Typography>
      </div>
      <div>
        <Typography variant="body2">Hello World</Typography>
      </div>
      <div>
        <Typography variant="body2Medium">Hello World</Typography>
      </div>
      <div>
        <Typography variant="button">Hello World</Typography>
      </div>
      <div>
        <Typography variant="caption">Hello World</Typography>
      </div>
      <div>
        <Typography variant="overline">Hello World</Typography>
      </div>
    </MainLayout>
  );
}
