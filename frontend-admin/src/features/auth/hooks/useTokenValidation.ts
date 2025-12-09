import { useIntrospectMutation } from '../api/authApi';

export const useTokenValidation = () => {
  const [introspect, { isLoading }] = useIntrospectMutation();

  const validateToken = async (token: string): Promise<boolean> => {
    try {
      const response = await introspect({ token }).unwrap();
      return response.result.valid;
    } catch (error) {
      console.error('Token validation error:', error);
      return false;
    }
  };

  return {
    validateToken,
    isValidating: isLoading,
  };
};
