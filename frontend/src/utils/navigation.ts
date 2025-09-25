import { createBrowserHistory } from 'history';

export type NavigationService = {
  navigate: (path: string) => void;
  history: ReturnType<typeof createBrowserHistory>;
};

export const history = createBrowserHistory();

export const createStandaloneNavigation = () => {
  return {
    navigate: (path: string) => history.push(path),
    history
  };
};