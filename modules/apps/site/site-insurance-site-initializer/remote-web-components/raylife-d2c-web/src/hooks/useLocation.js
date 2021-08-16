import { useEffect, useState } from "react";
import { GoogleMapsService } from "../services/google-maps";
import { MockService } from "../services/mock";

export const useLocation = () => {
  const [data, setData] = useState();
  const [error, setError] = useState();

  useEffect(() => {
    _loadUSStates();
  }, []);

  const _loadUSStates = async () => {
    try {
      const response = await MockService.getUSStates();
      setData(response);
    } catch (error) {
      console.warn(error);
      setError(error);
    }
  };

  const setAutoComplete = (htmlElement, callback) => {
    try {
      const autocomplete = GoogleMapsService.autocomplete(htmlElement);
      const infoWindow = GoogleMapsService.InfoWindow();

      autocomplete.addListener("place_changed", () => {
        infoWindow.close();
        const address = GoogleMapsService.getAutocompletePlaces(autocomplete);
        callback(address);
      });
    } catch (error) {
      console.warn(error);
    }
  };

  return {
    states: data || [],
    isLoading: !data && !error,
    isError: error,
    setAutoComplete,
  };
};
