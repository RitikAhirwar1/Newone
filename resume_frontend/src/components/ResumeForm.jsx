const handleSubmit = async () => {
    try {
        setLoading(true);
        const result = await generateResume(userDescription);
        // Handle success
    } catch (error) {
        if (error.code === 'ERR_NETWORK') {
            console.error('Network error: Unable to connect to the server');
            // Show user-friendly error message
            setError('Unable to connect to the server. Please check your internet connection or try again later.');
        } else {
            console.error('Error generating resume:', error);
            setError('An error occurred while generating the resume.');
        }
    } finally {
        setLoading(false);
    }
};