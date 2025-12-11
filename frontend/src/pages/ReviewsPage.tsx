import { useEffect, useState, useMemo } from 'react';
import {
  Container,
  Typography,
  Button,
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Chip,
  CircularProgress,
  Alert,
  FormControlLabel,
  Checkbox,
  Select,
  FormControl,
  InputLabel,
} from '@mui/material';
import { Add } from '@mui/icons-material';
import { evaluationService } from '@/services/evaluationService';
import { employeeService } from '@/services/employeeService';
import { ManagerEvaluation, EvaluationRequest, Employee } from '@/types';
import { useAuth } from '@/contexts/AuthContext';
import { format } from 'date-fns';

const ReviewsPage = () => {
  const [allReviews, setAllReviews] = useState<ManagerEvaluation[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [periodFilter, setPeriodFilter] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(15);
  const { user } = useAuth();
  const isHR = user?.role?.toLowerCase() === 'hr';

  const [formData, setFormData] = useState<EvaluationRequest>({
    employeeId: 0,
    reviewPeriod: '',
    score: 0,
    comment: '',
    nominatedForPromotion: false,
  });

  useEffect(() => {
    if (user?.employeeId) {
      if (isHR) {
        fetchEmployees();
        fetchAllReviews();
      } else {
        fetchReviews(user.employeeId);
      }
    } else {
      setLoading(false);
    }
  }, [user?.employeeId, isHR]);

  const fetchAllReviews = async () => {
    try {
      setLoading(true);
      const data = await evaluationService.getAllEvaluations();
      setAllReviews(data);
    } catch (error: any) {
      console.error('Failed to fetch reviews:', error);
      alert(error.response?.data?.message || 'Failed to fetch reviews');
      setAllReviews([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchReviews = async (employeeId: number) => {
    try {
      setLoading(true);
      const data = await evaluationService.getEvaluationsByEmployeeId(employeeId);
      setAllReviews(data);
    } catch (error: any) {
      console.error('Failed to fetch reviews:', error);
      alert(error.response?.data?.message || 'Failed to fetch reviews');
      setAllReviews([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchEmployees = async () => {
    try {
      const data = await employeeService.getAllEmployees();
      setEmployees(data);
    } catch (error) {
      console.error('Failed to fetch employees:', error);
    }
  };

  // Get unique review periods for filter dropdown
  const uniquePeriods = useMemo(() => {
    const periods = new Set(allReviews.map(r => r.reviewPeriod));
    return Array.from(periods).sort();
  }, [allReviews]);

  // Apply search and period filters
  const filteredReviews = useMemo(() => {
    return allReviews.filter(review => {
      // Search filter - search in employee name
      const matchesSearch = searchQuery === '' || 
        (review.employeeName || '').toLowerCase().includes(searchQuery.toLowerCase());
      
      // Period filter
      const matchesPeriod = periodFilter === '' || review.reviewPeriod === periodFilter;
      
      return matchesSearch && matchesPeriod;
    });
  }, [allReviews, searchQuery, periodFilter]);

  const handleOpenDialog = () => {
    setFormData({
      employeeId: 0,
      reviewPeriod: '',
      score: 0,
      comment: '',
      nominatedForPromotion: false,
    });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    if (type === 'number') {
      setFormData({ ...formData, [name]: value === '' ? 0 : Number(value) });
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const handleSelectChange = (e: any) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, nominatedForPromotion: e.target.checked });
  };

  const handleSubmit = async () => {
    // Validate required fields
    if (!formData.employeeId) {
      alert('Please select an employee');
      return;
    }
    if (!formData.reviewPeriod.trim()) {
      alert('Please enter a review period');
      return;
    }
    if (formData.score < 0 || formData.score > 100) {
      alert('Score must be between 0 and 100');
      return;
    }
    if (!formData.comment.trim()) {
      alert('Please enter a comment');
      return;
    }

    try {
      await evaluationService.createEvaluation(formData);
      alert('Review created successfully');
      handleCloseDialog();
      fetchAllReviews();
    } catch (error: any) {
      console.error('Failed to save review:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Failed to save review. Please check all fields are filled correctly.';
      alert(errorMessage);
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 90) return 'success';
    if (score >= 70) return 'primary';
    if (score >= 50) return 'warning';
    return 'error';
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container>
      {!user?.employeeId && (
        <Box sx={{ mb: 2 }}>
          <Alert severity="info">
            Session needs refresh. Please log out and log in again to load reviews.
          </Alert>
        </Box>
      )}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 3,
          gap: 2,
        }}
      >
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>
            Reviews History
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Track evaluations and promotion signals across the team
          </Typography>
        </Box>
        {isHR && (
          <Button variant="contained" startIcon={<Add />} onClick={handleOpenDialog}>
            Create Review
          </Button>
        )}
      </Box>

      {isHR && (
        <Box
          sx={{
            display: 'flex',
            gap: 2,
            mb: 3,
            flexWrap: 'wrap',
          }}
        >
          <TextField
            label="Search by Employee Name"
            variant="outlined"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            sx={{ flex: 1 }}
            placeholder="Enter employee name..."
          />
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>Review Period</InputLabel>
            <Select
              value={periodFilter}
              onChange={(e) => setPeriodFilter(e.target.value)}
              label="Review Period"
            >
              <MenuItem value="">All Periods</MenuItem>
              {uniquePeriods.map((period) => (
                <MenuItem key={period} value={period}>
                  {period}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      )}

      <Paper sx={{ overflow: 'hidden' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Review Period</TableCell>
                <TableCell>Employee</TableCell>
                <TableCell>Evaluator</TableCell>
                <TableCell align="right">Score (out of 100)</TableCell>
                <TableCell>Comment</TableCell>
                <TableCell>Nominated for Promotion</TableCell>
                <TableCell>Date</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredReviews.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    {searchQuery || periodFilter ? 'No reviews match the filters.' : 'No reviews found.'}
                  </TableCell>
                </TableRow>
              ) : (
                filteredReviews
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((review) => (
                  <TableRow key={review.id} hover>
                    <TableCell>{review.reviewPeriod}</TableCell>
                    <TableCell>{review.employeeName || `Employee #${review.employeeId}`}</TableCell>
                    <TableCell>{review.evaluatorName || `Evaluator #${review.evaluatorId}`}</TableCell>
                    <TableCell align="right">
                      <Chip
                        label={`${review.score}/100`}
                        color={getScoreColor(review.score)}
                        size="small"
                        variant="filled"
                      />
                    </TableCell>
                    <TableCell>{review.comment}</TableCell>
                    <TableCell>
                      <Chip
                        label={review.nominatedForPromotion ? 'Yes' : 'No'}
                        color={review.nominatedForPromotion ? 'success' : 'default'}
                        size="small"
                        variant={review.nominatedForPromotion ? 'filled' : 'outlined'}
                      />
                    </TableCell>
                    <TableCell>{format(new Date(review.createdAt), 'MMM dd, yyyy')}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          component="div"
          count={filteredReviews.length}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          rowsPerPageOptions={[15, 25, 50]}
        />
      </Paper>

      {isHR && (
        <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
          <DialogTitle>Create New Review</DialogTitle>
          <DialogContent>
            <FormControl fullWidth margin="normal" required>
              <InputLabel>Employee</InputLabel>
              <Select
                name="employeeId"
                value={formData.employeeId}
                onChange={handleSelectChange}
                label="Employee"
              >
                {employees.map((employee) => (
                  <MenuItem key={employee.id} value={employee.id}>
                    {employee.fullName} ({employee.email})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              fullWidth
              label="Review Period"
              name="reviewPeriod"
              value={formData.reviewPeriod}
              onChange={handleChange}
              margin="normal"
              placeholder="e.g., Q4-2024, 2024"
              required
            />
            <TextField
              fullWidth
              label="Score (0-100)"
              name="score"
              type="number"
              value={formData.score}
              onChange={handleChange}
              margin="normal"
              inputProps={{ min: 0, max: 100 }}
              required
            />
            <TextField
              fullWidth
              label="Comment"
              name="comment"
              value={formData.comment}
              onChange={handleChange}
              margin="normal"
              multiline
              rows={4}
              required
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.nominatedForPromotion}
                  onChange={handleCheckboxChange}
                />
              }
              label="Nominated for Promotion"
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDialog}>Cancel</Button>
            <Button onClick={handleSubmit} variant="contained" color="primary">
              Create
            </Button>
          </DialogActions>
        </Dialog>
      )}
    </Container>
  );
};

export default ReviewsPage;
